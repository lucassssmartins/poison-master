package br.com.poison.core.proxy.message;

import br.com.poison.core.Core;
import br.com.poison.core.proxy.ProxyCore;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class AutoMessage {

    private final ProxyCore proxy;

    private final int MESSAGE_DELAY_TIME = 120;

    private final List<String> messages = Arrays.asList(
            "mensagem 1",
            "mensagem 2"
    );

    /**
     * Run auto message's.
     */
    public void run() {
        proxy.getProxy().getScheduler().schedule(proxy, new Runnable() {
            final int messageSize = messages.size();

            int time = MESSAGE_DELAY_TIME;

            int index = 0;

            @Override
            public void run() {
                time -= 1;

                if (time == 0) {
                    Core.getMultiService().broadcast(messages.get(index));

                    index += 1;
                    time = MESSAGE_DELAY_TIME;

                    if (index >= messageSize) {
                        index = 0;
                    }

                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}