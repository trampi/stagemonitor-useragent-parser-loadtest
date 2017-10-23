package org.stagemonitor;

import org.junit.Test;
import org.stagemonitor.web.servlet.useragent.UAP;
import org.stagemonitor.web.servlet.useragent.UserAgentParser;
import org.stagemonitor.web.servlet.useragent.UserAgentParserNonCached;
import org.stagemonitor.web.servlet.useragent.UserAgentParserSynchronizedBlock;
import org.stagemonitor.web.servlet.useragent.UserAgentParserSynchronizedBlock2;
import org.stagemonitor.web.servlet.useragent.UserAgentParserSynchronizedMap;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;

public class LoadTest {

	public static final int THREADCOUNT = 4;
	public static final int BENCHMARK_RUNS = 3;
	DecimalFormat decimalFormat = new DecimalFormat(".##");

	@Test
	public void loadTestUserAgentParser() throws IOException, InterruptedException, URISyntaxException {

		List<UAP> uaps = Arrays.<UAP>asList(
				new UserAgentParser(),
				new UserAgentParserSynchronizedBlock(),
				new UserAgentParserSynchronizedBlock2(),
				new UserAgentParserSynchronizedMap(),
				new UserAgentParserNonCached()
		);

		for (UAP uap : uaps) {
			System.out.println("======== BEFORE uap " + uap.getClass().getSimpleName());
			System.out.println("======== BEFORE uap " + uap.getClass().getSimpleName());
			System.out.println("======== BEFORE uap " + uap.getClass().getSimpleName());
			System.out.println("======== BEFORE uap " + uap.getClass().getSimpleName());
			runBenchmark(uap);
			System.out.println("======== AFTER uap " + uap.getClass().getSimpleName());
			System.out.println("======== AFTER uap " + uap.getClass().getSimpleName());
			System.out.println("======== AFTER uap " + uap.getClass().getSimpleName());
			System.out.println("======== AFTER uap " + uap.getClass().getSimpleName());
		}

	}

	private void runBenchmark(final UAP userAgentParser) throws InterruptedException {
		final MockTracer mockTracer = new MockTracer();
		for (int i = 0; i < BENCHMARK_RUNS; i++) {
			System.out.println("setting up...");
			final List<LiveLocker> liveLockers = new ArrayList<>();
			for (int threadNumber = 0; threadNumber < THREADCOUNT; threadNumber++) {
				final List<String> userAgents = UserAgents.allUserAgents();
				LiveLocker liveLocker = new LiveLocker(threadNumber, userAgents, mockTracer, userAgentParser);
				liveLocker.setName(threadNumber + "");
				liveLocker.setDaemon(true);
				liveLockers.add(liveLocker);
			}
			System.out.println("Setup complete");

			for (Thread thread : liveLockers) {
				thread.start();
			}

			Thread.currentThread().sleep(10_000);

			for (Thread thread : liveLockers) {
				thread.stop();
				thread.join();
			}

			Thread.currentThread().sleep(500);

			long min = Long.MAX_VALUE;
			long max = Long.MIN_VALUE;
			long minCount = Long.MAX_VALUE;
			long maxCount = Long.MIN_VALUE;
			BigInteger count = BigInteger.ZERO;
			for (LiveLocker liveLocker : liveLockers) {
				min = Math.min(min, liveLocker.lastUpdate);
				max = Math.max(max, liveLocker.lastUpdate);
				minCount = Math.min(minCount, liveLocker.count);
				maxCount = Math.max(maxCount, liveLocker.count);
				count = count.add(BigInteger.valueOf(liveLocker.count));
			}
			double mean = new BigDecimal(count).divide(BigDecimal.valueOf(THREADCOUNT), BigDecimal.ROUND_UP).doubleValue();
			double variance = 0;
			for (LiveLocker liveLocker : liveLockers) {
				variance += (liveLocker.count-mean)*(liveLocker.count-mean);
			}
			double stddev = Math.sqrt(variance/(THREADCOUNT - 1));

			System.out.println("=========================");
			System.out.println("run: " + i + " (" + userAgentParser.getClass().getSimpleName() + ")");
			System.out.println("max - min: " + (max - min));
			System.out.println("now - min = " + (System.currentTimeMillis() - min));
			System.out.println("SUM: " + count);
			System.out.println("Mean: " + decimalFormat.format(mean));
			System.out.println("Stddev: " + decimalFormat.format(stddev));
			System.out.println("min: " + minCount);
			System.out.println("max: " + maxCount);
			System.out.println("=========================");
		}
	}


	class LiveLocker extends Thread {

		private final int threadId;
		private final List<String> userAgents;
		private final MockTracer mockTracer;
		private final UAP userAgentParser;

		public Long count = Long.MIN_VALUE;
		public Long lastUpdate = Long.MIN_VALUE;

		public LiveLocker(int threadId, List<String> userAgents, MockTracer mockTracer, UAP userAgentParser) {
			this.threadId = threadId;
			this.userAgents = userAgents;
			this.mockTracer = mockTracer;
			this.userAgentParser = userAgentParser;
		}

		public void run() {
			long lastUpdate = System.currentTimeMillis();
			long count = 0;
			try {
				//System.out.println(threadId + " starting");
				while (true) {
					for (int i = 0; i < userAgents.size(); i++) {
						try {
							MockSpan span = mockTracer.buildSpan("span").start();
							userAgentParser.setUserAgentInformation(span, userAgents.get(i));
							lastUpdate = System.currentTimeMillis();
							count++;
						} catch (Exception e) {
							System.out.println("exception in " + Thread.currentThread().getName());
							System.err.println(e.getMessage());
							e.printStackTrace();
							System.out.println("continuing");
						}
					}
				}
			} catch (ThreadDeath e) {
				this.lastUpdate = lastUpdate;
				this.count = count;
				//System.out.println(threadId + " dieing, count = " + count);
				throw e;
			}
		}

	}


}
