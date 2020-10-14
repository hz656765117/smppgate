package com.hz.smsgate.base.smpp.utils;

import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.business.smpp.impl.SmppClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CircularList {

    private List<SmppClient> collection = Collections.synchronizedList(new ArrayList<SmppClient>(20));


    public int size() {
        return collection.size();
    }

    public SmppClient fetchOne() {
        int size = collection.size();
        if (size == 0)
            return null;

        try {
            SmppClient ret = collection.get(0);
            return ret;
        } catch (IndexOutOfBoundsException ex) {
            // 多线程情况可能抛异常
            // 1：当线连接数为0了
            // 2：当前连接数小于index
            return collection.isEmpty() ? null : collection.get(0);
        } finally {
        }
    }

    public SmppClient fetchAndRemoveMaxOne() {
        int size = collection.size();
        if (size == 0)
            return null;

        try {
            SmppClient ret = collection.get(size-1);
            collection.remove(ret);
            return ret;
        } catch (IndexOutOfBoundsException ex) {
            // 多线程情况可能抛异常
            // 1：当线连接数为0了
            // 2：当前连接数小于index
            return collection.isEmpty() ? null : collection.get(0);
        } finally {
        }
    }

    public SmppClient fetch() {
        int size = collection.size();
        if (size == 0)
            return null;

        int idx = indexSeq.incrementAndGet();

        try {
            SmppClient ret = collection.get((idx & 0xffff) % size);
            return ret;
        } catch (IndexOutOfBoundsException ex) {
            // 多线程情况可能抛异常
            // 1：当线连接数为0了
            // 2：当前连接数小于index
            return collection.isEmpty() ? null : collection.get(0);
        } finally {
        }
    }

    public List<SmppClient> getAll(){
        return collection;
    }

    public boolean add(SmppClient ele) {

        boolean r = false;
        try {
            r = collection.add(ele);
        } finally {
        }
        return r;
    }

    public boolean remove(SmppClient ele) {

        boolean r = false;
        try {
            r = collection.remove(ele);
        } finally {
        }
        return r;
    }

    private AtomicInteger indexSeq = new AtomicInteger();

}
