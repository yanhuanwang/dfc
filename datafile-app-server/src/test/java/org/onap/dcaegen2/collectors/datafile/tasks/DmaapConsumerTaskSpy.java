/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.dcaegen2.collectors.datafile.tasks;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.onap.dcaegen2.collectors.datafile.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.collectors.datafile.configuration.AppConfig;
import org.onap.dcaegen2.collectors.datafile.service.consumer.ExtendedDmaapConsumerHttpClientImpl;
import org.onap.dcaegen2.collectors.datafile.tasks.DmaapConsumerTaskImpl;
import org.onap.dcaegen2.collectors.datafile.tasks.Task;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author <a href="mailto:henrik.b.andersson@est.tech">Henrik Andersson</a> on 3/27/18
 */
@Configuration
public class DmaapConsumerTaskSpy {

    @Bean
    @Primary
    public Task registerSimpleDmaapConsumerTask() {
        AppConfig appConfig = spy(AppConfig.class);
        doReturn(mock(DmaapConsumerConfiguration.class)).when(appConfig).getDmaapConsumerConfiguration();
        DmaapConsumerTaskImpl dmaapConsumerTask = spy(new DmaapConsumerTaskImpl(appConfig));
        ExtendedDmaapConsumerHttpClientImpl extendedDmaapConsumerHttpClient = mock(
            ExtendedDmaapConsumerHttpClientImpl.class);
        doReturn(mock(DmaapConsumerConfiguration.class)).when(dmaapConsumerTask).resolveConfiguration();
        doReturn(extendedDmaapConsumerHttpClient).when(dmaapConsumerTask).resolveClient();
        return dmaapConsumerTask;
    }
}
