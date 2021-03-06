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
package fr.insalyon.creatis.vip.api.bean;

import fr.insalyon.creatis.vip.api.bean.pairs.IntKeyStringValuePair;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Tristan Glatard
 */
@XmlType(name = "GlobalProperties")
public class GlobalProperties extends Object {

    @XmlElement(name = "APIErrorCodesAndMessages")
    private ArrayList<IntKeyStringValuePair> APIErrorCodesAndMessages;

    @XmlElement(name = "supportedTransferProtocol", required=true)
    private ArrayList<String> supportedTransferProtocols;

    @XmlElement(name = "supportedModule", required=true)
    private ArrayList<Module> supportedModules;
    
    @XmlElement(name = "email")
    private String email;
    
    @XmlElement(name = "platformDescription")
    private String platformDescription;
    
    @XmlElement(name = "minAuthorizedExecutionTimeout")
    private Integer minAuthorizedExecutionTimeout;
    
    @XmlElement(name = "maxAuthorizedExecutionTimeout")
    private Integer maxAuthorizedExecutionTimeout;
    
    @XmlElement(name = "defaultExecutionTimeout")
    private Integer defaultExecutionTimeout;
    
    @XmlElement(name = "isKillExecutionSupported")
    private Boolean isKillExecutionSupported;
    
    @XmlElement(name = "defaultStudy")
    private String defaultStudy;
   
    @XmlElement(name = "supportedAPIVersion", required=true)
    private String supportedAPIVersion;
    
    public GlobalProperties() {
        this.APIErrorCodesAndMessages = new ArrayList<>();
        this.supportedModules = new ArrayList<>();
        this.supportedTransferProtocols = new ArrayList<>();
    }

    public GlobalProperties(String email, String platformDescription, Integer minAuthorizedExecutionTimeout, Integer maxAuthorizedExecutionTimeout, Integer defaultExecutionTimeout, Boolean isKillExecutionSupported, String defaultStudy, String supportedAPIVersion) {
        this();
        this.email = email;
        this.platformDescription = platformDescription;
        this.minAuthorizedExecutionTimeout = minAuthorizedExecutionTimeout;
        this.maxAuthorizedExecutionTimeout = maxAuthorizedExecutionTimeout;
        this.defaultExecutionTimeout = defaultExecutionTimeout;
        this.isKillExecutionSupported = isKillExecutionSupported;
        this.defaultStudy = defaultStudy;
        this.supportedAPIVersion = supportedAPIVersion;
    }

    public ArrayList<IntKeyStringValuePair> getAPIErrorCodesAndMessages() {
        return APIErrorCodesAndMessages;
    }

    public ArrayList<String> getSupportedTransferProtocols() {
        return supportedTransferProtocols;
    }

    public ArrayList<Module> getSupportedModules() {
        return supportedModules;
    }

}
