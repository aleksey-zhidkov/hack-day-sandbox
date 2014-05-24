import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author a.maximov
 * @since 24.05.2014
 */
public class Util
{
	public static Map<String, String> toMap(String json) throws IOException
	{
		return new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>(){});
	}

	public static List<Map<String, String>> toList(String json) throws IOException
	{
		return new ObjectMapper().readValue(json, new TypeReference<List<Map<String, Object>>>(){});
	}
}
