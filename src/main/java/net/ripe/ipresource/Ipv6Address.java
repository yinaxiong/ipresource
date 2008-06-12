package net.ripe.ipresource;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;

/**
 * Ipv6 address. This implementation has no support for interfaces.
 * 
 * @author Serge Beaumont
 *
 */
public class Ipv6Address extends IpAddress {

	/**
	 * Mask for 16 bits, which is the length of one part of an IPv6 address.
	 */
	private BigInteger PART_MASK = BigInteger.valueOf(65535);
	
	protected Ipv6Address(IpResourceType type, BigInteger value) {
		super(type, value);
	}

    public Ipv6Address(BigInteger value) {
		super(IpResourceType.IPv6, value);
	}

	public static Ipv6Address parse(String ipAddressString) {
        Validate.isTrue(Pattern.matches("[0-9a-fA-F]{0,4}:([0-9a-fA-F]{0,4}:){1,6}[0-9a-fA-F]{0,4}", ipAddressString), "Invalid IPv6 address: " + ipAddressString);
        
        // Count number of colons: must be between 2 and 7
        int colonCount = countColons(ipAddressString);
        int doubleColonCount = numberOfDoubleColons(ipAddressString);
      
        // The number of double colons must be exactly one if there's a missing colon.
        // The double colon will be the place that gets filled out to complete the address for easy parsing.
        if (colonCount < 7) {
        	Validate.isTrue(doubleColonCount == 1, "May only be one double colon in an IPv6 address");

        	// Add extra colons
        	ipAddressString = expandColons(ipAddressString);
        }
        
        // By now we have an IPv6 address that's guaranteed to have 7 colons.
        
        return new Ipv6Address(IpResourceType.IPv6, ipv6StringtoBigInteger(ipAddressString));
    }

	/**
	 * Converts a fully expanded IPv6 string to a BigInteger
	 * 
	 * @param Fully expanded address (i.e. no '::' shortcut)
	 * @return Address as BigInteger
	 */
	private static BigInteger ipv6StringtoBigInteger(String ipAddressString) {
		Pattern p = Pattern.compile("([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4})");
        Matcher m = p.matcher(ipAddressString);
        m.find();
        
        String ipv6Number = "";
        for (int i = 1; i <= m.groupCount(); i++) {
        	String part = m.group(i);
        	String padding = "0000".substring(0, 4 - part.length());
			ipv6Number = ipv6Number + padding + part;
		}

		return new BigInteger(ipv6Number, 16);
	}

    public String toString(boolean defaultMissingOctets) {
    	String[] parts = new String[8];
    	
    	for (int i = 0; i < parts.length; i++) {
    		BigInteger part = getValue().shiftRight(i*16).and(PART_MASK);
    		if (BigInteger.ZERO.equals(part)) {
    			parts[i] = "";
    		} else {
    			parts[i] = part.toString(16);	
    		}
		}

    	String result = String.format("%s:%s:%s:%s:%s:%s:%s:%s",
    			parts[7],
    			parts[6],
    			parts[5],
    			parts[4],
    			parts[3],
    			parts[2],
    			parts[1],
    			parts[0]);
    	
    	result = compressColons(result);
    	
    	return result;
    }
	

    // -------------------------------------------------------------------------------- HELPERS
    
	private String compressColons(String ipv6Address) {
		// Compress colons into short notation
    	ipv6Address = ipv6Address.replaceAll(":{3,7}", "::");
		return ipv6Address;
	}		

	private static String expandColons(String ipv6String) {
		String filledDoubleColons = ":::::::".substring(0, 7 - countColons(ipv6String) + 2);
		ipv6String = ipv6String.replace("::", filledDoubleColons);
		return ipv6String;
	}

	private static int countColons(String ipv6String) {
		Pattern colonPattern = Pattern.compile(":");
        Matcher colonMatcher = colonPattern.matcher(ipv6String);
        int colonCount = 0;
        while (colonMatcher.find()) { colonCount++ ; };
		return colonCount;
	}

	private static int numberOfDoubleColons(String ipv6String) {
		// Count number of double colons: should be either 0 (with 7 colons) or 1 (with less)
        Pattern doubleColonPattern = Pattern.compile("::");
        Matcher doubleColonMatcher = doubleColonPattern.matcher(ipv6String);
        int doubleColonCount = 0;
        while (doubleColonMatcher.find()) { doubleColonCount++ ; };
		return doubleColonCount;
	}	
	
}
