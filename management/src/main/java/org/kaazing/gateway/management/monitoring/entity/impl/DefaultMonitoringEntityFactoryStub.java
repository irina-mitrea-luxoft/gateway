/**
 * Copyright (c) 2007-2014 Kaazing Corporation. All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.kaazing.gateway.management.monitoring.entity.impl;

import org.kaazing.gateway.management.monitoring.entity.BooleanMonitoringEntity;
import org.kaazing.gateway.management.monitoring.entity.LongMonitoringCounter;
import org.kaazing.gateway.management.monitoring.entity.StringMonitoringEntity;
import org.kaazing.gateway.management.monitoring.entity.factory.MonitoringEntityFactory;

public class DefaultMonitoringEntityFactoryStub implements MonitoringEntityFactory {

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public BooleanMonitoringEntity makeBooleanMonitoringEntity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LongMonitoringCounter makeLongMonitoringCounter(String name) {
        // TODO Auto-generated method stub
        return new DefaultLongMonitoringCounterStub();
    }

    @Override
    public StringMonitoringEntity makeStringMonitoringEntity() {
        // TODO Auto-generated method stub
        return null;
    }

}