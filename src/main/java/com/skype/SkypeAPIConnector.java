/**
 * Mule Development Kit
 * Copyright 2010-2011 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This file was automatically generated by the Mule Development Kit
 */
package com.skype;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.io.FileUtils;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Connect;
import org.mule.api.annotations.ValidateConnection;
import org.mule.api.annotations.ConnectionIdentifier;
import org.mule.api.annotations.Disconnect;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.param.ConnectionKey;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.api.ConnectionException;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skype.api.Account;
import com.skype.api.Account.STATUS;
import com.skype.api.Conversation;
import com.skype.api.Skype;
import com.skype.api.Sms;
import com.skype.ipc.TCPSocketTransport;
import com.skype.ipc.TLSServerTransport;
import com.skype.ipc.Transport;
import com.skype.skypekitclient.utils.Ask;
import com.skype.util.PemReader;

/**
 * Cloud Connector
 * 
 * @author MuleSoft, Inc.
 */
@Connector(name = "skypeapi", schemaVersion = "1.0-SNAPSHOT")
public class SkypeAPIConnector {
	
	private static Logger logger = LoggerFactory.getLogger(SkypeAPIConnector.class);
	
	
	/**
	 * Server IP Address.
	 * 
	 * @since 1.0
	 */
	public static final String IP_ADDR = "127.0.0.1";

	/**
	 * Server Port. <br />
	 * <br />
	 * If you modify this compiled-in default, you will need to start the
	 * matching SkypeKit runtime with option:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;<code>-p <em>9999</em></code><br />
	 * where <code>-p <em>9999</em></code> reflects this value.
	 * 
	 * @since 1.0
	 */
	public static final int PORT_NUM = 8963;
	
	/**
	 * The path where the pem certificate is located
	 */
	@Configurable
	@Optional
	@Default(value="Version 1.0-SNAPSHOT.pem")
	private String pemFileName;
	/**
	 * Skype username
	 */
	@Configurable
	private String username;
	/**
	 * Skype password
	 */
	@Configurable
	private String password;
	

	private Skype skype;

	private TLSServerTransport transport;
	private MuleSkypeWrapper listener;


	private File pemFile;

	@Start
	public void createSkypeConnector() {
		this.skype = new Skype();
		this.pemFile = FileUtils.toFile(this.getClass().getClassLoader().getResource(pemFileName));
		this.listener = new MuleSkypeWrapper();
	}

	/**
	 * Connect
	 * 
	 * @param username
	 *            A username
	 * @param password
	 *            A password
	 * @throws ConnectionException
	 */
	@Connect
	public void connect() throws ConnectionException {

		try {
			PemReader donkey = new PemReader(pemFile.getAbsolutePath());
			X509Certificate c = donkey.getCertificate();
			PrivateKey p = donkey.getKey();
			Transport t = new TCPSocketTransport(IP_ADDR, PORT_NUM);
			transport = new TLSServerTransport(t, c, p);
			skype.Init(transport);
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Disconnect
	 */
	@Disconnect
	public void disconnect() {
		try {
			skype.Close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Are we connected
	 */
	@ValidateConnection
	public boolean isConnected() {
		return true;
	}

	/**
	 * Are we connected
	 */
	@ConnectionIdentifier
	public String connectionId() {
		return "001";
	}

	/**
	 * Custom processor
	 * 
	 * {@sample.xml ../../../doc/SkypeAPI-connector.xml.sample
	 * skypeapi:my-processor}
	 * 
	 * @param message
	 *            Content to be texted
	 * @param target
	 * 			  The recipient of the text
	 * 
	 */
	@Processor
	public void myProcessor(String message,String target) {
		
		
		Conversation conversation = skype.GetConversationByIdentity(target);
        if (conversation != null) {
            Sms sms = skype.CreateOutgoingSms();
            boolean result = false;
            
            if (sms == null) {
            	logger.warn("can not create sms");
                
            }
                                                    
            Sms.SetSMSBodyResult smsBodyResult = sms.SetBody(message);
            if (smsBodyResult == null) {
            	logger.warn("can not set body");
            }
                    
            String[] targets = new String[1]; 
            targets[0] = target; 
            result = sms.SetTargets(targets);
            if (!result) {
            	logger.warn("can not set target number");
                return;
            }                                  
                           
            conversation.PostSMS(sms, "");
//            if (!conversation.PostSMS(sms, "")) {
//            	skClient.error("can not send sms to participants of " + conversation.GetStrProp(Conversation.P_META_TOPIC));
//            } 
                                   
            logger.info("\nsms sent");                
        } else {
        	logger.warn("unable to create conversation from identity " + target); 
        } 
	}

	public void setCallActive(boolean b) {
		// TODO Auto-generated method stub

	}

	public void setLoginStatus(STATUS accountStatus) {
		// TODO Auto-generated method stub

	}

	public Account getAccount() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPemFileName() {
		return pemFileName;
	}

	public void setPemFileName(String pemFileName) {
		this.pemFileName = pemFileName;
	}
}
