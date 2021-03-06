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
package fr.insalyon.creatis.vip.api.rest.itest;

import fr.insalyon.creatis.vip.api.bean.Module;
import fr.insalyon.creatis.vip.api.rest.config.BaseVIPSpringIT;
import fr.insalyon.creatis.vip.api.rest.config.RestTestUtils;
import fr.insalyon.creatis.vip.api.rest.model.SupportedTransferProtocol;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import static fr.insalyon.creatis.vip.api.data.CarminAPITestConstants.*;
import static fr.insalyon.creatis.vip.api.data.ErrorCodeAndMessageTestUtils.jsonCorrespondsToErrorCodeAndMessage;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by abonnet on 7/20/16.
 *
 * Test method on platform path
 */
public class PlatformControllerIT extends BaseVIPSpringIT {

    @Test
    public void platformShouldNotBeSecured() throws Exception {
        mockMvc.perform(get("/rest/platform"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testPlatformProperties() throws Exception {
        // the test properties are set in BaseVIPSpringIT (with @TestPropertySource)
        mockMvc.perform(get("/rest/platform"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(RestTestUtils.JSON_CONTENT_TYPE_UTF8))
                .andExpect(jsonPath("$.platformName")
                        .value(TEST_PLATFORM_NAME))
                .andExpect(jsonPath("$.platformDescription")
                        .value(TEST_PLATFORM_DESCRIPTION))
                .andExpect(jsonPath("$.email")
                        .value(TEST_PLATFORM_EMAIL))
                .andExpect(jsonPath("$.supportedTransferProtocols[*]",
                        isArray(TEST_SUPPORTED_PROTOCOLS, SupportedTransferProtocol::toValue)))
                .andExpect(jsonPath("$.supportedModules[*]",
                        isArray(TEST_SUPPORTED_MODULES, Module::getLabel)))
                .andExpect(jsonPath("$.defaultLimitListExecutions")
                        .value(Integer.valueOf(TEST_DEFAULT_LIST_LIMIT)))
                .andExpect(jsonPath("$.unsupportedMethods[*]",
                        isArray(TEST_UNSUPPORTED_METHOD, String::toString)))
                .andExpect(jsonPath("$.supportedAPIVersion")
                        .value(TEST_SUPPORTED_API_VERSION))
                .andExpect(jsonPath("$.maxSizeDirectTransfer")
                        .value(Integer.valueOf(TEST_DATA_MAX_SIZE)))
                // it should be a long, be the test value being small it's actually an int
                .andExpect(jsonPath("$.APIErrorCodesAndMessages[*]",
                        containsInAnyOrder(
                                jsonCorrespondsToErrorCodeAndMessage(TEST_ERROR_CODES_AND_MESSAGES[0]),
                                jsonCorrespondsToErrorCodeAndMessage(TEST_ERROR_CODES_AND_MESSAGES[1]))));

    }

    private <T> Matcher<Collection<String>> isArray(T[] values, Function<T,String> mapper) {
        // magic java 8 to convert enum array to string array
        String[] enumAsStringArray = Arrays.stream(values).map(mapper).toArray(String[]::new);
        return allOf(
                Matchers.<String>hasSize(values.length),
                Matchers.containsInAnyOrder(enumAsStringArray) );
    }
}
