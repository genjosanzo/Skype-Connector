package com.skype;

import java.util.Locale;

import com.skype.api.Account.PROPERTY;
import com.skype.api.ContactGroup;
import com.skype.api.Conversation;
import com.skype.api.Conversation.LIST_TYPE;
import com.skype.api.Conversation.LOCAL_LIVESTATUS;
import com.skype.api.Message;
import com.skype.api.Participant.DTMF;
import com.skype.api.Skype;
import com.skype.api.SkypeObject;
import com.skype.api.Video;
import com.skype.skypekitclient.SkypekitListeners.jwcObserver;

public class MuleSkypeWrapper extends Skype implements jwcObserver{
	/**
	 * Info/Debug console output message prefix/identifier tag <em>prefix</em>.
	 * Corresponds to class name.
	 * 
	 * @since 1.0
	 */
    public static final String MY_CLASS_TAG_PFX = "MuleSkype";
    
	/**
	 * Info/Debug console output message prefix/identifier tag.
	 * Corresponds to class name.
	 * 
	 * @since 1.0
	 */
    public String muleSkypeTag;
    
	/**
	 * Datagram stream ID, used by Tutorial 11.
	 * 
	 * @since 1.0
	 */
	public String streamName = new String("");


    /**
     * Default constructor.
     * <br/><br />
     * Forces the info/debug console output message prefix/identifier tag to:<br />
     * &nbsp;&nbsp;<code>MY_CLASS_TAG_PFX + "0"</code> (zero).
     * 
	 * @since 1.0
     */
    public MuleSkypeWrapper() {
    	
    	this((MY_CLASS_TAG_PFX + "0"));
    }


    /**
     * Tutorial constructor.
     * <br/><br />
     * Sets the info/debug console output message prefix/identifier tag to
     * <code>MY_CLASS_TAG_PFX</code> concatenated with the portion of the
     * invoker's prefix/identifier tag that starts with an underscore. These
     * characters should be the digits corresponding to the tutorial step number.
     * 
	 * @since 1.0
     */
    public MuleSkypeWrapper(String tutorialTag) {

     	muleSkypeTag = MY_CLASS_TAG_PFX + tutorialTag.substring(tutorialTag.indexOf('_'));
    }

    
	/**
	 * Assigns active input and output devices from among those available.
	 * Notifies user regarding the name of the selected devices or whether the request failed.
	 * <em>Both</em> devices must exist for the request to succeed.
	 * 
	 * @param micIdx
	 * 	Index into the array of available recording devices of the requested input device.
	 * @param spkrIdx
	 * 	Index into the array of available playback devices of the requested output device.
	 * 
	 * @return
	 * <ul>
	 *   <li>true: success</li>
	 *   <li>false: failure</li>
	 * </ul>
	 * 
	 * @see com.skype.api.Skype#GetAvailableRecordingDevices()
	 * @see com.skype.api.Skype#GetAvailableOutputDevices()
	 * 
	 * @since 1.0
	 */
	public boolean SetupAudioDevices(int micIdx, int spkrIdx) {
		boolean	passFail = true;	// Ever the optimist, assume success!
		
		Skype.GetAvailableRecordingDevicesResult inputDevices = this.GetAvailableRecordingDevices();
		Skype.GetAvailableOutputDevicesResult outputDevices = this.GetAvailableOutputDevices();

		if (micIdx > (inputDevices.handleList.length + 1)) {
			System.out.printf("%s: Invalid mic device no. (%d) passed!%n", muleSkypeTag, micIdx);
			passFail = false;
		}

		if (spkrIdx > (outputDevices.handleList.length + 1)) {
			System.out.printf("%s: Invalid speaker device no. (%d) passed!%n", muleSkypeTag, spkrIdx);
			passFail = false;
		}
		
		if (passFail) {
			System.out.printf("%s: Setting mic to %s (%s)%n",
					muleSkypeTag, inputDevices.nameList[micIdx], inputDevices.productIdList[micIdx]);
			System.out.printf("%s: Setting speakers to %s  (%s)%n",
					muleSkypeTag, outputDevices.nameList[spkrIdx], outputDevices.productIdList[spkrIdx]);
			this.SelectSoundDevices(inputDevices.handleList[micIdx],
					outputDevices.handleList[spkrIdx], outputDevices.handleList[spkrIdx]);
			this.SetSpeakerVolume(100);
		}

		return (passFail);
	}
	
	
	/**
	 * Normalizes a phone number and indicates that operation's success/failure.
	 * <br /><br />
	 * Determines the country code dialing prefix through {@link com.skype.api.Skype#GetISOCountryInfo()}
	 * by matching the default Locale country with an entry in the
	 * {@link com.skype.api.Skype.GetISOCountryInfoResult#countryCodeList}.
	 * Writes a message to the console indicating success/failure reason.
	 * 
	 * @param pstn
	 * 	Phone number to normalize.
	 * 
	 * @return
	 *   The normalization result, which includes:
	 *   <ul>
	 *     <li>an Enum instance detailing success/failure reason.</li>
	 *     <li>the normalized string (success) or error message string (failure)</li>
	 *   </ul>
	 * 
	 * @see com.skype.api.Skype#NormalizePSTNWithCountry(String, int)
	 * @see com.skype.api.Skype#GetISOCountryInfo()
	 * 
	 * @since 1.0
	 */
	public Skype.NormalizeIdentityResult GetNormalizationStr(String pstn) {
		Skype.NormalizeIdentityResult nrmlResultReturn = new NormalizeIdentityResult();
		
		Skype.GetISOCountryInfoResult isoInfo = this.GetISOCountryInfo();
		int availCountryCodes = isoInfo.countryCodeList.length;
		int isoInfoIdx;
		String ourCountryCode = Locale.getDefault().getCountry();
		for (isoInfoIdx = 0; isoInfoIdx < availCountryCodes; isoInfoIdx++) {
			if (ourCountryCode.equalsIgnoreCase(isoInfo.countryCodeList[isoInfoIdx])) {
				break;
			}
		}
		if (isoInfoIdx >= availCountryCodes) {
			nrmlResultReturn.result = Skype.NORMALIZERESULT.IDENTITY_EMPTY; // Anything but IDENTITY_OK...
			nrmlResultReturn.normalized = "Couldn't match Locale!";
			System.out.printf("%s: Error! Couldn't match Locale %s in Skype.GetISOCountryInfo results%n",
					muleSkypeTag, ourCountryCode);
			return (nrmlResultReturn);
		}
		System.out.printf("%n%s ISOInfo match (%d of %d):%n\tCode: %s%n\tDialExample: %s%n\tName: %s%n\tPrefix: %s%nLocale: %s%n%n",
				muleSkypeTag, (isoInfoIdx + 1),
				this.GetISOCountryInfo().countryCodeList.length,
				this.GetISOCountryInfo().countryCodeList[isoInfoIdx],
				this.GetISOCountryInfo().countryDialExampleList[isoInfoIdx],
				this.GetISOCountryInfo().countryNameList[isoInfoIdx],
				this.GetISOCountryInfo().countryPrefixList[isoInfoIdx],
				Locale.getDefault().getCountry());
		
		Skype.NormalizePSTNWithCountryResult nrmlResult =
			this.NormalizePSTNWithCountry(pstn, isoInfo.countryPrefixList[isoInfoIdx]);

		switch (nrmlResult.result) {
		case IDENTITY_OK:
			nrmlResultReturn.normalized = nrmlResult.normalized;
			break;
		case IDENTITY_EMPTY:
			nrmlResultReturn.normalized = "Identity input was empty";
			break;
		case IDENTITY_TOO_LONG:
			nrmlResultReturn.normalized = "Identity string too long";
			break;
		case IDENTITY_CONTAINS_INVALID_CHAR:
			nrmlResultReturn.normalized = "Invalid character(s) found in identity string";
			break;
		case PSTN_NUMBER_TOO_SHORT:
			nrmlResultReturn.normalized = "PSTN number too short";
			break;
		case PSTN_NUMBER_HAS_INVALID_PREFIX:
			nrmlResultReturn.normalized = "Invalid character(s) found in PSTN prefix";
			break;
		case SKYPENAME_STARTS_WITH_NONALPHA :
			nrmlResultReturn.normalized = "Skype Name string starts with non-alphanumeric character";
			break;
		case SKYPENAME_SHORTER_THAN_6_CHARS:
			nrmlResultReturn.normalized = "Skype Name too short";
			break;
		default:
			nrmlResultReturn.normalized = "Cannot determine Skype.NORMALIZATION ?!?";
			break;
		}

		if (nrmlResult.result != Skype.NORMALIZERESULT.IDENTITY_OK) {
			System.out.printf("%s: Error! Raw PSTN: %s - Normalized PSTN: %s.%n", muleSkypeTag, pstn, nrmlResultReturn.normalized);
		}
		else {
			System.out.printf("%s: Raw PSTN: %s / Normalized PSTN: %s.%n",
									muleSkypeTag, pstn, nrmlResultReturn.normalized);
		}
		
		nrmlResultReturn.result = nrmlResult.result;
		return nrmlResultReturn;
	}


	@Override
	public void OnConversationListChange(Conversation conversation,
			LIST_TYPE type, boolean added) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnIncomingDTMF(SkypeObject obj, DTMF dtmf) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnMessage(Message message, boolean changesInboxTimestamp,
			Message supersedesHistoryMessage, Conversation conversation) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnNewCustomContactGroup(ContactGroup group) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnPropertyChange(SkypeObject obj, PROPERTY prop, Object value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnPropertyChange(SkypeObject obj,
			com.skype.api.Contact.PROPERTY prop, Object value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnPropertyChange(SkypeObject obj,
			com.skype.api.ContactGroup.PROPERTY prop, Object value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnPropertyChange(SkypeObject obj,
			com.skype.api.ContactSearch.PROPERTY prop, Object value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnPropertyChange(SkypeObject obj,
			com.skype.api.Conversation.PROPERTY prop, Object value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnPropertyChange(SkypeObject obj,
			com.skype.api.Message.PROPERTY prop, Object value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnPropertyChange(SkypeObject obj,
			com.skype.api.Participant.PROPERTY prop, Object value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnPropertyChange(SkypeObject obj,
			com.skype.api.Sms.PROPERTY prop, Object value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnPropertyChange(SkypeObject obj,
			com.skype.api.Transfer.PROPERTY prop, Object value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnPropertyChange(SkypeObject obj,
			com.skype.api.Video.PROPERTY prop, Object value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnPropertyChange(SkypeObject obj,
			com.skype.api.Voicemail.PROPERTY prop, Object value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onAccountStatusChange() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onLiveStatusChange(Conversation conv, LOCAL_LIVESTATUS status) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onVideoDimensionsChange(Video v, String value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onVideoErrorChange(Video v, String error) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnH264Activated(SkypeObject obj) {
		// TODO Auto-generated method stub
		
	}

/*
 	 **
	 * Translates an APP2APP_STREAMS type to a displayable string.
	 * Used by Tutorial 11.
	 * 
	 * @param listType
	 * 	APP2APP_STREAMS enum to translate.
	 * 
	 * @return
	 *   A string representation of the enum value, or "unknown stream type" if not recognized.
	 * 
	 * @see com.skype.api.Skype.APP2APP_STREAMS
	 * 
	 * @since 1.0
	 *
	public String StreamListType(Skype.APP2APP_STREAMS listType) {
		String listTypeAsText;
		
	    switch (listType) {
	        case ALL_STREAMS:
	        	listTypeAsText = "all streams";
	        	break;
	        case SENDING_STREAMS:
	        	listTypeAsText = "sending stream";
	        	break;
	        case RECEIVED_STREAMS:
	        	listTypeAsText = "receiving stream";
	        	break;
	        default:
	        	listTypeAsText = "unknown stream type";
	        	break;
	    }
	    return (listTypeAsText);
	}
*/
}
