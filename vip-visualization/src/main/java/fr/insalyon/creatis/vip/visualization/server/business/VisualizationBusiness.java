/*
 * Copyright and authors: see LICENSE.txt in base repository.
 *
 * This software is a web portal for pipeline execution on distributed systems.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package fr.insalyon.creatis.vip.visualization.server.business;

import fr.insalyon.creatis.grida.client.GRIDAClient;
import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.business.CoreUtil;
import fr.insalyon.creatis.vip.datamanager.client.view.DataManagerException;
import fr.insalyon.creatis.vip.datamanager.server.DataManagerUtil;
import fr.insalyon.creatis.vip.visualization.client.bean.Image;
import fr.insalyon.creatis.vip.visualization.client.bean.VisualizationItem;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisualizationBusiness {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Image getImageSlicesURL(String imageFileName, String dir)
        throws BusinessException {

        File imageFile = new File(imageFileName);
        String imageDirName = imageFile.getParent() + "/"
            + imageFile.getName() + "-" + dir + "-slices";
        File imageDir = new File(imageDirName);

        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        String sliceZeroFileName = imageDirName + "/slice0.png";
        File sliceZero = new File(sliceZeroFileName);
        if (!sliceZero.exists()) {
            //split slices
            ProcessBuilder builder = new ProcessBuilder(
                "slice.sh", imageFileName, imageDirName, dir);
            builder.redirectErrorStream(true);
            try {
                builder.start();
                try {
                    // wait for the first slice to be produced but not for all
                    // slices ;)
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException ex) {
                    logger.error("slice.sh waiting interrupted", ex);
                    new File(imageDirName).delete();
                    throw new BusinessException(ex);
                }
            } catch (IOException ex) {
                logger.error("Error running slice.sh command for {}",
                        imageFileName, ex);
                new File(imageDirName).delete();
                throw new BusinessException(ex);
            }
        }
        //get z value
        ProcessBuilder builderZ =
            new ProcessBuilder("getz.sh", imageFileName, dir);
        builderZ.redirectErrorStream(true);
        String number = "";
        try {
            try {
                Process process = builderZ.start();
                process.waitFor();

                InputStream stdout = process.getInputStream();
                BufferedReader reader =
                    new BufferedReader(new InputStreamReader(stdout));

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Stdout: " + line);
                    number += line;
                }
            } catch (InterruptedException ex) {
                logger.error("getz.sh waiting interrupted", ex);
                new File(imageDirName).delete();
                throw new BusinessException(ex);
            }

        } catch (IOException ex) {
            logger.error("Error running getz.sh command for {}",
                    imageFileName, ex);
            new File(imageDirName).delete();
            throw new BusinessException(ex);
        }

        logger.info("IMAGE DIR NAME IS " + imageDirName);
        return new Image(
            imageDirName,
            Integer.parseInt(number.trim()),
            imageDirName.substring(
                imageDirName.indexOf("/files/viewer")) + "/");
    }

    public VisualizationItem getVisualizationItemFromLFN(
        String lfn, String localDir, User user, Connection connection)
        throws BusinessException {

        String separator = System.getProperty("file.separator");

        String relativeDirString;
        try {
            relativeDirString = "files/viewer"
                + (new File(DataManagerUtil.parseBaseDir(user, lfn, connection))
                   .getParent()
                   .replaceAll(" ", "_")
                   .replaceAll("\\([^\\(]*\\)", ""));
        } catch (DataManagerException ex) {
            throw new BusinessException(ex);
        }

        String fileDirString = localDir
            + separator
            + relativeDirString;
        File fileDir = new File(fileDirString);
        String fileName = localFilename(lfn, fileDir);

        if (!fileDir.exists()) {
            fileDir.mkdirs();
            if (!fileDir.exists()) {
                logger.error("Cannot create viewer dir: {}", fileDir.getAbsolutePath());
                throw new BusinessException(
                    "Cannot create viewer dir: " + fileDir.getAbsolutePath());
            }
        }
        fileDir.setWritable(true, false);

        GRIDAClient gridaClient = CoreUtil.getGRIDAClient();

        if (!(new File(fileName)).exists()) {
            logger.info("Downloading file: " + lfn);
            try {
                gridaClient.getRemoteFile(
                    DataManagerUtil.parseBaseDir(user, lfn, connection),
                    fileDir.getAbsolutePath());
            } catch (GRIDAClientException ex) {
                logger.error("Error getting file {}", lfn, ex);
                fileDir.delete();
                throw new BusinessException(ex);
            } catch (DataManagerException ex) {
                throw new BusinessException(ex);
            }
        } else {
            logger.info("File already in cache: " + lfn);
        }

        // Hack: if it is a .mhd file, download also the raw file with the same
        // name, testing multiple possible extensions.
        String rawFileExtension = "";
        if (lfn.endsWith(".mhd")) {
            rawFileExtension =
                rawFileForMhdFile(gridaClient, lfn, user, fileDir, connection)
                .orElse("");
        }

        String url = relativeDirString
            + separator
            + lfn.substring(lfn.lastIndexOf('/') + 1);

        return new VisualizationItem(url, fileName, rawFileExtension);
    }

    private Optional<String> rawFileForMhdFile(
        GRIDAClient gridaClient,
        String lfn,
        User user,
        File localDir,
        Connection connection) {

        String[] extensions = {".raw", ".zraw", ".raw.gz"};

        return java.util.Arrays.stream(extensions)
            .map(extension -> buildLfnName(user, lfn, extension, connection))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(fe -> checkIfExists(gridaClient, fe.filename))
            .findFirst()
            .filter(fe -> downloadFileSucceeds(
                        gridaClient, fe.filename, localDir))
            .map(fe -> fe.extension);
    }

    private Optional<FilenameAndExtension> buildLfnName(
        User user, String lfn, String extension, Connection connection) {
        try {
            return Optional.of(
                new FilenameAndExtension(
                    DataManagerUtil.parseBaseDir(
                        user, lfn.replaceAll("\\.mhd$", extension), connection),
                    extension));
        } catch (DataManagerException dme) {
            logger.warn("Error while building lfn name with new extension: {}. Ignoring", lfn);
            return Optional.empty();
        }
    }

    private boolean checkIfExists(GRIDAClient gridaClient, String filename) {
        try {
            return gridaClient.exist(filename);
        } catch (GRIDAClientException gce) {
            logger.warn("Error while grida checked existance of: {}", filename, gce);
            return false;
        }
    }

    private boolean downloadFileSucceeds(
        GRIDAClient gridaClient, String filename, File localDir) {
        try {
            if (!new File(localFilename(filename, localDir)).exists()) {
                logger.info("Downloading file: " + filename);
                gridaClient.getRemoteFile(filename, localDir.getAbsolutePath());
            } else {
                logger.info("File already in cache: " + filename);
            }
            return true;
        } catch (GRIDAClientException gce) {
            logger.warn("Error while grida downloading file: {}", filename, gce);
            return false;
        }
    }

    private String localFilename(String filename, File localDir) {
        return localDir.getAbsolutePath()
            + '/'
            + filename.substring(filename.lastIndexOf('/') + 1);
    }

    private static class FilenameAndExtension {
        public final String filename;
        public final String extension;
        public FilenameAndExtension(String filename, String extension) {
            this.filename = filename;
            this.extension = extension;
        }
    }
}
