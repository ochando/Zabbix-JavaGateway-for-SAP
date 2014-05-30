package com.zabbix.gateway;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.jmx.ObjectNameFactory;
import com.sap.jmx.remote.JmxConnectionFactory;

public class JavaGatewayTest {

	private static final Logger logger = LoggerFactory.getLogger(JavaGatewayTest.class);

	public static void main(String[] args) throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException {
		if (args.length != 5) {
			System.out.println("Usage: JavaGateway [ip] [port] [user] [passwd] [sap cluster id]");
			System.exit(0);
		}

		String url = args[0] + ":" + args[1];
		String username = args[2];
		String password = args[3];
		String clusterId = args[4];

		logger.debug("connecting to JMX agent at {}", url);

		// set the connection properties for the RMI-P4 connection
		Properties connectionProperties = new Properties();
		connectionProperties.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				"com.sap.engine.services.jndi.InitialContextFactoryImpl");
		connectionProperties.setProperty(Context.PROVIDER_URL, url);
		if ("-" != username && "-" != password) {
			connectionProperties.setProperty(Context.SECURITY_PRINCIPAL, username);
			connectionProperties.setProperty(Context.SECURITY_CREDENTIALS, password);
		}

		// create the MBeanServerConnection
		MBeanServerConnection mbsc = JmxConnectionFactory.getMBeanServerConnection(
				JmxConnectionFactory.PROTOCOL_ENGINE_P4, connectionProperties);

		ObjectName pattern = ObjectNameFactory.getPatternForServerChildPerNode(clusterId, null);

		Set<ObjectName> names = mbsc.queryNames(pattern, null);

		StringBuilder stringBuilder = new StringBuilder(); 
		for (ObjectName name : names) {
			stringBuilder.append("------------------------Object Details------------------------");
			stringBuilder.append(name.toString());
			stringBuilder.append("Domain: "+ name.getDomain());
			stringBuilder.append("Canonical Name: "+ name.getCanonicalName());
			stringBuilder.append("Canonical Key Property List: "+ name.getCanonicalKeyPropertyListString());
			
			stringBuilder.append("------------------------Object Attribute Details------------------------");
			MBeanInfo mBeanInfo = mbsc.getMBeanInfo(name);
			MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
			for (MBeanAttributeInfo mBeanAttributeInfo : attributes) {
				stringBuilder.append("MBean Attribute Description: " + mBeanAttributeInfo.getDescription());
				stringBuilder.append("MBean Attribute Name: " + mBeanAttributeInfo.getName());
				stringBuilder.append("MBean Attribute Type: " + mBeanAttributeInfo.getType());
				stringBuilder.append("MBean Attribute Is Readable: " + mBeanAttributeInfo.isReadable());
				stringBuilder.append("------------------------");
			}
			
			stringBuilder.append("\n\n\n");
		}
		System.out.println(stringBuilder);

	}

}
