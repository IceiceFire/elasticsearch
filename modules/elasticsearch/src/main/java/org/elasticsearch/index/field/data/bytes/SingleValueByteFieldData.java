/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.field.data.bytes;

import org.elasticsearch.common.RamUsage;
import org.elasticsearch.common.thread.ThreadLocals;
import org.elasticsearch.index.field.data.doubles.DoubleFieldData;

/**
 * @author kimchy (shay.banon)
 */
public class SingleValueByteFieldData extends ByteFieldData {

    private ThreadLocal<ThreadLocals.CleanableValue<double[]>> doublesValuesCache = new ThreadLocal<ThreadLocals.CleanableValue<double[]>>() {
        @Override protected ThreadLocals.CleanableValue<double[]> initialValue() {
            return new ThreadLocals.CleanableValue<double[]>(new double[1]);
        }
    };

    private ThreadLocal<ThreadLocals.CleanableValue<byte[]>> valuesCache = new ThreadLocal<ThreadLocals.CleanableValue<byte[]>>() {
        @Override protected ThreadLocals.CleanableValue<byte[]> initialValue() {
            return new ThreadLocals.CleanableValue<byte[]>(new byte[1]);
        }
    };

    // order with value 0 indicates no value
    private final int[] ordinals;

    public SingleValueByteFieldData(String fieldName, int[] ordinals, byte[] values) {
        super(fieldName, values);
        this.ordinals = ordinals;
    }

    @Override protected long computeSizeInBytes() {
        return super.computeSizeInBytes() +
                RamUsage.NUM_BYTES_INT * ordinals.length + RamUsage.NUM_BYTES_ARRAY_HEADER;
    }

    @Override public boolean multiValued() {
        return false;
    }

    @Override public boolean hasValue(int docId) {
        return ordinals[docId] != 0;
    }

    @Override public void forEachValueInDoc(int docId, StringValueInDocProc proc) {
        int loc = ordinals[docId];
        if (loc == 0) {
            return;
        }
        proc.onValue(docId, Byte.toString(values[loc]));
    }

    @Override public void forEachValueInDoc(int docId, DoubleValueInDocProc proc) {
        int loc = ordinals[docId];
        if (loc == 0) {
            return;
        }
        proc.onValue(docId, values[loc]);
    }

    @Override public void forEachValueInDoc(int docId, ValueInDocProc proc) {
        int loc = ordinals[docId];
        if (loc == 0) {
            return;
        }
        proc.onValue(docId, values[loc]);
    }

    @Override public byte value(int docId) {
        return values[ordinals[docId]];
    }

    @Override public double[] doubleValues(int docId) {
        int loc = ordinals[docId];
        if (loc == 0) {
            return DoubleFieldData.EMPTY_DOUBLE_ARRAY;
        }
        double[] ret = doublesValuesCache.get().get();
        ret[0] = values[loc];
        return ret;
    }

    @Override public byte[] values(int docId) {
        int loc = ordinals[docId];
        if (loc == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] ret = valuesCache.get().get();
        ret[0] = values[loc];
        return ret;
    }
}