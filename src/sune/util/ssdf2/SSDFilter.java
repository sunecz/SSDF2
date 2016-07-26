package sune.util.ssdf2;

import static sune.util.ssdf2.SSDType.BOOLEAN;
import static sune.util.ssdf2.SSDType.DECIMAL;
import static sune.util.ssdf2.SSDType.INTEGER;
import static sune.util.ssdf2.SSDType.NULL;
import static sune.util.ssdf2.SSDType.STRING;
import static sune.util.ssdf2.SSDType.UNKNOWN;

@FunctionalInterface
public interface SSDFilter {
	
	// Pre-defined filters
	public static final SSDFilter ONLY_COLLECTIONS = ((n) -> n.isCollection());
	public static final SSDFilter ONLY_OBJECTS 	   = ((n) -> n.isObject());
	
	public static final SSDFilter ONLY_NULLS
		= ((n) -> (n.isObject()) && ((SSDObject) n).getType() == NULL);
	public static final SSDFilter ONLY_BOOLEANS
		= ((n) -> (n.isObject()) && ((SSDObject) n).getType() == BOOLEAN);
	public static final SSDFilter ONLY_INTEGERS
		= ((n) -> (n.isObject()) && ((SSDObject) n).getType() == INTEGER);
	public static final SSDFilter ONLY_DECIMALS
		= ((n) -> (n.isObject()) && ((SSDObject) n).getType() == DECIMAL);
	public static final SSDFilter ONLY_STRINGS
		= ((n) -> (n.isObject()) && ((SSDObject) n).getType() == STRING);
	public static final SSDFilter ONLY_UNKNOWNS
		= ((n) -> (n.isObject()) && ((SSDObject) n).getType() == UNKNOWN);
	
	// Methods
	boolean accept(SSDNode node);
}