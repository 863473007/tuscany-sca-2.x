/*
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

package org.apache.tuscany.sca.binding.corba.testing.generated;

/**
* org/apache/tuscany/sca/binding/corba/testing/generated/_TestObjectStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from general_tests.idl
* monday, 23 june 2008 14:12:28 CEST
*/

public class _TestObjectStub extends org.omg.CORBA.portable.ObjectImpl implements
    org.apache.tuscany.sca.binding.corba.testing.generated.TestObject {

    public org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct pickStructFromArgs(org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct arg1,
                                                                                                org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct arg2,
                                                                                                org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct arg3,
                                                                                                int structNumber) {
        org.omg.CORBA.portable.InputStream $in = null;
        try {
            org.omg.CORBA.portable.OutputStream $out = _request("pickStructFromArgs", true);
            org.apache.tuscany.sca.binding.corba.testing.generated.SomeStructHelper.write($out, arg1);
            org.apache.tuscany.sca.binding.corba.testing.generated.SomeStructHelper.write($out, arg2);
            org.apache.tuscany.sca.binding.corba.testing.generated.SomeStructHelper.write($out, arg3);
            $out.write_long(structNumber);
            $in = _invoke($out);
            org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct $result =
                org.apache.tuscany.sca.binding.corba.testing.generated.SomeStructHelper.read($in);
            return $result;
        } catch (org.omg.CORBA.portable.ApplicationException $ex) {
            $in = $ex.getInputStream();
            String _id = $ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        } catch (org.omg.CORBA.portable.RemarshalException $rm) {
            return pickStructFromArgs(arg1, arg2, arg3, structNumber);
        } finally {
            _releaseReply($in);
        }
    } // pickStructFromArgs

    public org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct setStruct(org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct arg) {
        org.omg.CORBA.portable.InputStream $in = null;
        try {
            org.omg.CORBA.portable.OutputStream $out = _request("setStruct", true);
            org.apache.tuscany.sca.binding.corba.testing.generated.SomeStructHelper.write($out, arg);
            $in = _invoke($out);
            org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct $result =
                org.apache.tuscany.sca.binding.corba.testing.generated.SomeStructHelper.read($in);
            return $result;
        } catch (org.omg.CORBA.portable.ApplicationException $ex) {
            $in = $ex.getInputStream();
            String _id = $ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        } catch (org.omg.CORBA.portable.RemarshalException $rm) {
            return setStruct(arg);
        } finally {
            _releaseReply($in);
        }
    } // setStruct

    public org.apache.tuscany.sca.binding.corba.testing.generated.SimpleStruct setSimpleStruct(org.apache.tuscany.sca.binding.corba.testing.generated.SimpleStructHolder arg) {
        org.omg.CORBA.portable.InputStream $in = null;
        try {
            org.omg.CORBA.portable.OutputStream $out = _request("setSimpleStruct", true);
            org.apache.tuscany.sca.binding.corba.testing.generated.SimpleStructHelper.write($out, arg.value);
            $in = _invoke($out);
            org.apache.tuscany.sca.binding.corba.testing.generated.SimpleStruct $result =
                org.apache.tuscany.sca.binding.corba.testing.generated.SimpleStructHelper.read($in);
            arg.value = org.apache.tuscany.sca.binding.corba.testing.generated.SimpleStructHelper.read($in);
            return $result;
        } catch (org.omg.CORBA.portable.ApplicationException $ex) {
            $in = $ex.getInputStream();
            String _id = $ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        } catch (org.omg.CORBA.portable.RemarshalException $rm) {
            return setSimpleStruct(arg);
        } finally {
            _releaseReply($in);
        }
    } // setSimpleStruct

    public int[] setLongSeq1(org.apache.tuscany.sca.binding.corba.testing.generated.long_seq1Holder arg) {
        org.omg.CORBA.portable.InputStream $in = null;
        try {
            org.omg.CORBA.portable.OutputStream $out = _request("setLongSeq1", true);
            org.apache.tuscany.sca.binding.corba.testing.generated.long_seq1Helper.write($out, arg.value);
            $in = _invoke($out);
            int $result[] = org.apache.tuscany.sca.binding.corba.testing.generated.long_seq1Helper.read($in);
            arg.value = org.apache.tuscany.sca.binding.corba.testing.generated.long_seq1Helper.read($in);
            return $result;
        } catch (org.omg.CORBA.portable.ApplicationException $ex) {
            $in = $ex.getInputStream();
            String _id = $ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        } catch (org.omg.CORBA.portable.RemarshalException $rm) {
            return setLongSeq1(arg);
        } finally {
            _releaseReply($in);
        }
    } // setLongSeq1

    public int[][] setLongSeq2(org.apache.tuscany.sca.binding.corba.testing.generated.long_seq2Holder arg) {
        org.omg.CORBA.portable.InputStream $in = null;
        try {
            org.omg.CORBA.portable.OutputStream $out = _request("setLongSeq2", true);
            org.apache.tuscany.sca.binding.corba.testing.generated.long_seq2Helper.write($out, arg.value);
            $in = _invoke($out);
            int $result[][] = org.apache.tuscany.sca.binding.corba.testing.generated.long_seq2Helper.read($in);
            arg.value = org.apache.tuscany.sca.binding.corba.testing.generated.long_seq2Helper.read($in);
            return $result;
        } catch (org.omg.CORBA.portable.ApplicationException $ex) {
            $in = $ex.getInputStream();
            String _id = $ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        } catch (org.omg.CORBA.portable.RemarshalException $rm) {
            return setLongSeq2(arg);
        } finally {
            _releaseReply($in);
        }
    } // setLongSeq2

    public int[][][] setLongSeq3(org.apache.tuscany.sca.binding.corba.testing.generated.long_seq3Holder arg) {
        org.omg.CORBA.portable.InputStream $in = null;
        try {
            org.omg.CORBA.portable.OutputStream $out = _request("setLongSeq3", true);
            org.apache.tuscany.sca.binding.corba.testing.generated.long_seq3Helper.write($out, arg.value);
            $in = _invoke($out);
            int $result[][][] = org.apache.tuscany.sca.binding.corba.testing.generated.long_seq3Helper.read($in);
            arg.value = org.apache.tuscany.sca.binding.corba.testing.generated.long_seq3Helper.read($in);
            return $result;
        } catch (org.omg.CORBA.portable.ApplicationException $ex) {
            $in = $ex.getInputStream();
            String _id = $ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        } catch (org.omg.CORBA.portable.RemarshalException $rm) {
            return setLongSeq3(arg);
        } finally {
            _releaseReply($in);
        }
    } // setLongSeq3

    // Type-specific CORBA::Object operations
    private static String[] __ids = {"IDL:org/apache/tuscany/sca/binding/corba/testing/generated/TestObject:1.0"};

    @Override
    public String[] _ids() {
        return (String[])__ids.clone();
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException {
        String str = s.readUTF();
        String[] args = null;
        java.util.Properties props = null;
        org.omg.CORBA.Object obj = org.omg.CORBA.ORB.init(args, props).string_to_object(str);
        org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
        _set_delegate(delegate);
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        String[] args = null;
        java.util.Properties props = null;
        String str = org.omg.CORBA.ORB.init(args, props).object_to_string(this);
        s.writeUTF(str);
    }
} // class _TestObjectStub
