/* Copyright CNRS-CREATIS
 *
 * Rafael Silva
 * rafael.silva@creatis.insa-lyon.fr
 * http://www.creatis.insa-lyon.fr/~silva
 *
 * This software is a grid-enabled data-driven workflow manager and editor.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
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
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.insalyon.creatis.vip.datamanagement.client.view.menu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Item;
import com.gwtext.client.widgets.menu.Menu;
import com.gwtext.client.widgets.menu.event.BaseItemListenerAdapter;
import fr.insalyon.creatis.vip.common.client.view.Context;
import fr.insalyon.creatis.vip.datamanagement.client.view.panel.DownloadPanel;

/**
 *
 * @author Rafael Silva
 */
public class DownloadActionsMenu extends Menu {

    public DownloadActionsMenu() {
        this.setId("dm-download-actions-menu");

        // Download Selected Operations
        Item downloadSelectedItem = new Item("Download Selected Files", new BaseItemListenerAdapter() {

            @Override
            public void onClick(BaseItem item, EventObject e) {
                Record[] records = DownloadPanel.getInstance().getCbSelectionModel().getSelections();
                for (Record r : records) {
                    if (r.getAsString("typeico").equals("Done")) {
                        Window.open(
                                GWT.getModuleBaseURL()
                                + "/filedownloadservice?operationid="
                                + r.getAsString("operationid")
                                + "&proxy="
                                + Context.getInstance().getAuthentication().getProxyFileName(),
                                "", "");
                    }
                }
            }
        });

        this.addItem(downloadSelectedItem);
    }
}
