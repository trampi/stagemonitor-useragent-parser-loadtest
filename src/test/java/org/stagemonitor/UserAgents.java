package org.stagemonitor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class UserAgents {

	private static List<String> userAgentStrings;
	private static List<String> uniqueUserAgents;

	public static List<String> allUserAgents() {
		if (userAgentStrings == null) {
			initUserAgents();
		}
		return new ArrayList<>(userAgentStrings);
	}

	public static List<String> shuffledAllUserAgents() {
		List<String> allUserAgents = allUserAgents();
		Collections.shuffle(allUserAgents);
		return allUserAgents;
	}

	private static void initUserAgents() {
		try {
			userAgentStrings = Files.readAllLines(Paths.get(UserAgents.class.getResource("/user-agents.txt").toURI()), Charset.defaultCharset());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<String> uniqueUserAgents() {
		if (uniqueUserAgents == null) {
			uniqueUserAgents = new ArrayList<>(new HashSet<String>(allUserAgents()));
		}
		return new ArrayList<String>(uniqueUserAgents);
	}

	public static List<String> shuffledUniqueUserAgents() {
		final List<String> shuffledUniqueUserAgents = uniqueUserAgents();
		Collections.shuffle(shuffledUniqueUserAgents);
		return shuffledUniqueUserAgents;
	}

}
