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

import java.util.ArrayList;

import org.onap.dcaegen2.collectors.datafile.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.collectors.datafile.service.FileData;
import org.onap.dcaegen2.collectors.datafile.service.consumer.ExtendedDmaapConsumerHttpClientImpl;

/**
 * @author <a href="mailto:henrik.b.andersson@est.tech">Henrik Andersson</a> on 7/27/18
 */
abstract class DmaapConsumerTask<R, S, C> extends Task<R, S, C> {

    abstract ArrayList<FileData> consume(String message) throws DmaapNotFoundException;

    abstract ExtendedDmaapConsumerHttpClientImpl resolveClient();

    abstract void initConfigs();
}
