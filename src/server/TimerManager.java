/*
 	OrpheusMS: MapleStory Private Server based on OdinMS
    Copyright (C) 2012 Aaron Weiss <aaron@deviant-core.net>
    				Patrick Huy <patrick.huy@frz.cc>
					Matthias Butz <matze@odinms.de>
					Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package server;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class TimerManager implements TimerManagerMBean {
	private static TimerManager instance = new TimerManager();
	private ScheduledThreadPoolExecutor ses;

	private TimerManager() {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		try {
			mBeanServer.registerMBean(this, new ObjectName("server:type=TimerManger"));
		} catch (Exception e) {
		}
	}

	public static TimerManager getInstance() {
		return instance;
	}

	public void start() {
		if (ses != null && !ses.isShutdown() && !ses.isTerminated()) {
			return;
		}
		ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(4, new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("TimerManager-Worker-" + threadNumber.getAndIncrement());
				return t;
			}
		});
		// this is a no-no, it actually does nothing..then why the fuck are you
		// doing it?
		stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		ses = stpe;
	}

	public void stop() {
		ses.shutdownNow();
	}

	public Runnable purge() {// Yay?
		return new Runnable() {
			public void run() {
				ses.purge();
			}
		};
	}

	public ScheduledFuture<?> register(Runnable r, long repeatTime, long delay) {
		return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r), delay, repeatTime, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> register(Runnable r, long repeatTime) {
		return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r), 0, repeatTime, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> schedule(Runnable r, long delay) {
		return ses.schedule(new LoggingSaveRunnable(r), delay, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleAtTimestamp(Runnable r, long timestamp) {
		return schedule(r, timestamp - System.currentTimeMillis());
	}

	@Override
	public long getActiveCount() {
		return ses.getActiveCount();
	}

	@Override
	public long getCompletedTaskCount() {
		return ses.getCompletedTaskCount();
	}

	@Override
	public int getQueuedTasks() {
		return ses.getQueue().toArray().length;
	}

	@Override
	public long getTaskCount() {
		return ses.getTaskCount();
	}

	@Override
	public boolean isShutdown() {
		return ses.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return ses.isTerminated();
	}

	private static class LoggingSaveRunnable implements Runnable {
		Runnable r;

		public LoggingSaveRunnable(Runnable r) {
			this.r = r;
		}

		@Override
		public void run() {
			try {
				r.run();
			} catch (Throwable t) {
			}
		}
	}
}
