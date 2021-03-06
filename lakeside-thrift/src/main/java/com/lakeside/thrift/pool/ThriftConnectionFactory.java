package com.lakeside.thrift.pool;

import java.lang.ref.SoftReference;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.PooledSoftReference;
import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lakeside.thrift.ThriftConfig;
import com.lakeside.thrift.ThriftException;
import com.lakeside.thrift.host.ThriftHost;
import com.lakeside.thrift.host.ThriftHostManager;
import com.lakeside.thrift.pool.ThriftConnection.TServiceValidator;

/**
 * ThriftConnection Factory,
 * 用于创建一个到服务器的连接（thrift socket connection).
 * 
 * @author zhufb
 *
 */
public class ThriftConnectionFactory<T extends TServiceClient & TServiceValidator> extends BasePooledObjectFactory<ThriftConnection<T>> {
	
	private static final Logger log = LoggerFactory.getLogger("ThriftConnectionFactory");
    private ThriftHostManager hostManager;
	private BaseThriftConnectionPool<T> pool;
	
    public ThriftConnectionFactory(BaseThriftConnectionPool<T> pool,ThriftConfig cfg,ThriftHostManager hostManager){
    	this.pool = pool;
    	this.hostManager = hostManager;
    }
    
    ThriftConnectionFactory(ThriftConfig cfg,ThriftHostManager hostManager){
    	this.hostManager = hostManager;
    }

	/**
     * 关闭一个连接对象
     * @param obj
     */
    @Override
	public void destroyObject(PooledObject<ThriftConnection<T>> obj) {
		obj.getObject().destroy();
		obj = null;
	}
	
	 /**
	  * 校验连接对象(ThriftConnection)是否可用
	  * 
	  */
	@Override
	public boolean validateObject(PooledObject<ThriftConnection<T>> obj) {
		if (obj == null) {
			return false;
		}
		return obj.getObject().validate();
	}

	@Override
	public ThriftConnection<T> create() throws Exception {
		ThriftHost host = null;
		 try{
			 host = hostManager.get();
			 return new ThriftConnection<T>(pool, host);
		 }catch(ThriftException e){
			 log.error("This thrift server was not running " + host.getIp());
			 throw e;
		 }catch(Exception e){
			 log.error("This thrift server make new client failed " + host.getIp());
			 throw e;
		 }
	}

	@Override
	public PooledObject<ThriftConnection<T>> wrap(ThriftConnection<T> obj) {
		return new PooledSoftReference<ThriftConnection<T>>(new SoftReference<ThriftConnection<T>>(obj));
	}
}