package com.roncoo.pay;

import com.roncoo.pay.app.notify.core.NotifyPersist;
import com.roncoo.pay.app.notify.core.NotifyQueue;
import com.roncoo.pay.app.notify.core.NotifyTask;
import com.roncoo.pay.common.core.page.PageBean;
import com.roncoo.pay.common.core.page.PageParam;
import com.roncoo.pay.notify.entity.RpNotifyRecord;
import com.roncoo.pay.notify.service.RpNotifyService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.DelayQueue;

@SpringBootApplication
public class AppNotifyApplication {

    private static final Log LOG = LogFactory.getLog(AppNotifyApplication.class);

    public static DelayQueue<NotifyTask> tasks = new DelayQueue<NotifyTask>();

    @Autowired
    private ThreadPoolTaskExecutor threadPool;
    @Autowired
    public RpNotifyService rpNotifyService;
    @Autowired
    private NotifyQueue notifyQueue;
    @Autowired
    public NotifyPersist notifyPersist;


    private static ThreadPoolTaskExecutor cacheThreadPool;

    public static RpNotifyService cacheRpNotifyService;

    private static NotifyQueue cacheNotifyQueue;

    public static NotifyPersist cacheNotifyPersist;

    public static void main(String[] args) {
//        SpringApplication.run(AppNotifyApplication.class, args);
        new SpringApplicationBuilder().sources(AppNotifyApplication.class).web(WebApplicationType.NONE).run(args);
    }

    @PostConstruct
    public void init() {
        cacheThreadPool = threadPool;
        cacheRpNotifyService = rpNotifyService;
        cacheNotifyQueue = notifyQueue;
        cacheNotifyPersist = notifyPersist;

        startInitFromDB();
        startThread();
    }

    private static void startThread() {
        LOG.info("startThread");

        cacheThreadPool.execute(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(50);//50??????????????????
                        // ????????????????????????????????????????????????????????????
                        if (cacheThreadPool.getActiveCount() < cacheThreadPool.getMaxPoolSize()) {
                            final NotifyTask task = tasks.poll();
                            if (task != null) {
                                cacheThreadPool.execute(new Runnable() {
                                    public void run() {
                                        LOG.info(cacheThreadPool.getActiveCount() + "---------");
                                        tasks.remove(task);
                                        task.run();
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("????????????", e);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * ???????????????????????????????????????????????????????????????
     */
    @SuppressWarnings("unchecked")
    private static void startInitFromDB() {
        LOG.info("get data from database");

        int pageNum = 1;
        int numPerPage = 500;
        PageParam pageParam = new PageParam(pageNum, numPerPage);

        // ??????????????????????????????????????????????????????????????????
        String[] status = new String[]{"101", "102", "200", "201"};
        Integer[] notifyTime = new Integer[]{0, 1, 2, 3, 4};
        // ??????????????????
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("statusList", status);
        paramMap.put("notifyTimeList", notifyTime);

        PageBean<RpNotifyRecord> pager = cacheRpNotifyService.queryNotifyRecordListPage(pageParam, paramMap);
        int totalSize = (pager.getNumPerPage() - 1) / numPerPage + 1;//?????????
        while (pageNum <= totalSize) {
            List<RpNotifyRecord> list = pager.getRecordList();
            for (int i = 0; i < list.size(); i++) {
                RpNotifyRecord notifyRecord = list.get(i);
                notifyRecord.setLastNotifyTime(new Date());
                cacheNotifyQueue.addElementToList(notifyRecord);
            }
            pageNum++;
            LOG.info(String.format("??????????????????.rpNotifyService.queryNotifyRecordListPage(%s, %s, %s)", pageNum, numPerPage, paramMap));
            pageParam = new PageParam(pageNum, numPerPage);
            pager = cacheRpNotifyService.queryNotifyRecordListPage(pageParam, paramMap);
        }
    }


}

