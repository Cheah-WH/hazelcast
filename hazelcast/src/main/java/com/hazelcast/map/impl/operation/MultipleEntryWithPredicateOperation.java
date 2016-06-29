/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.map.impl.operation;

import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.query.Predicate;
import com.hazelcast.spi.Operation;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.hazelcast.util.Preconditions.checkNotNull;

public class MultipleEntryWithPredicateOperation extends MultipleEntryOperation {

    private Predicate predicate;

    public MultipleEntryWithPredicateOperation() {
    }

    public MultipleEntryWithPredicateOperation(String name, Set<Data> keys, EntryProcessor entryProcessor,
                                               Predicate predicate) {
        super(name, keys, entryProcessor);
        this.predicate = checkNotNull(predicate, "predicate cannot be null");
    }

    @Override
    protected boolean isEntryProcessable(Map.Entry entry) {
        return super.isEntryProcessable(entry) && predicate.apply(entry);
    }

    @Override
    public Operation getBackupOperation() {
        EntryBackupProcessor backupProcessor = entryProcessor.getBackupProcessor();
        MultipleEntryWithPredicateBackupOperation backupOperation
                = new MultipleEntryWithPredicateBackupOperation(name, keys, backupProcessor, predicate);
        backupOperation.setWanEventList(wanEventList);

        return backupOperation;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);

        out.writeObject(predicate);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);

        predicate = in.readObject();
    }
}
