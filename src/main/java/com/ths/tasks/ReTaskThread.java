package com.ths.tasks;

import com.ths.domain.DownloadBlock;
import com.ths.domain.ControlBean;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wangyonghao
 */
@Slf4j
public class ReTaskThread implements Runnable {

    /**线程内部变量，用来标记尝试请求失败次数*/
    private int count = 0;

    @Override
    public void run() {
        try {
            while (true) {
                DownloadBlock block = ControlBean.loseBlocks.take();
                ControlBean.waitBlocks.add(block);
                ControlBean.continueDownload = true;
                //有失败的文件块，把文件块放回去继续下载，等待3秒，观察3s内是否有下载成功的文件块(这个时间很关键)，
                Thread.sleep(3000L);
                //没有下载成功的文件块就停止下载（此时ControlBean.continueDownload为false，下载线程进不了下载任务的代码块）
                if (!ControlBean.continueDownload) {
                    count++;
                    log.warn("===>进入暂停下载模式");
                    log.info("===>休眠时间" + (count * 3) + "s");
                } else {
                    log.info("===>回复所有失败的文件块");
                    for (int i = 0; i < ControlBean.loseBlocks.size(); i++) {
                        ControlBean.waitBlocks.add(ControlBean.loseBlocks.take());
                    }
                    count = 0;
                }
                //进入休眠时间策略
                if (count < 20) {
                    //不休眠count = 0；20次以内的尝试，梯度休眠，超过20次，固定休眠60s
                    Thread.sleep(count * 3000L);
                } else {
                    //固定频率，1分钟试一次
                    Thread.sleep(60000L);
                }
            }

        } catch (InterruptedException e) {
            log.info("===>reTask线程出错...{}", e);
        }
    }
}
