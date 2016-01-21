/**
 * Copyright 2007-2015, Kaazing Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaazing.gateway.transport.wseb.specification.wse.connector;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.kaazing.test.util.ITUtil.timeoutRule;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.kaazing.gateway.transport.test.Expectations;
import org.kaazing.gateway.transport.wseb.test.WsebConnectorRule;
import org.kaazing.gateway.util.InternalSystemProperty;
import org.kaazing.k3po.junit.annotation.Specification;
import org.kaazing.k3po.junit.rules.K3poRule;
import org.kaazing.mina.core.session.IoSessionEx;
import org.kaazing.test.util.ITUtil;
import org.kaazing.test.util.MethodExecutionTrace;

public class ClosingIT {

    private K3poRule k3po = new K3poRule().setScriptRoot("org/kaazing/specification/wse/closing");

    private final WsebConnectorRule connector;

    {
        Properties configuration = new Properties();
        configuration.setProperty(InternalSystemProperty.WS_CLOSE_TIMEOUT.getPropertyName(), "2s");
        connector = new WsebConnectorRule(configuration);
    }

    private JUnitRuleMockery context = new JUnitRuleMockery() {
        {
            setThreadingPolicy(new Synchroniser());
        }
    };

    private TestRule contextRule = ITUtil.toTestRule(context);
    private final TestRule trace = new MethodExecutionTrace();
    private final TestRule timeoutRule = timeoutRule(15, SECONDS);

    @Rule
    public TestRule chain = RuleChain.outerRule(trace).around(connector).around(contextRule).around(k3po)
            .around(timeoutRule);

    @Ignore("#345: WsebConnector does not do clean close")
    @Test
    @Specification("client.send.close/response")
    public void shouldPerformClientInitiatedClose() throws Exception {
        final IoHandler handler = context.mock(IoHandler.class);
        final CountDownLatch closed = new CountDownLatch(1);

        context.checking(new Expectations() {
            {
                oneOf(handler).sessionCreated(with(any(IoSessionEx.class)));
                oneOf(handler).sessionOpened(with(any(IoSessionEx.class)));
                oneOf(handler).sessionClosed(with(any(IoSessionEx.class)));
                will(countDown(closed));
            }
        });

        ConnectFuture connectFuture = connector.connect("ws://localhost:8080/path?query", null, handler);
        assertTrue("Connect failed", connectFuture.await(5,  SECONDS));
        IoSessionEx connectSession = (IoSessionEx) connectFuture.getSession();
        connectSession.close(false);
        assertTrue(closed.await(4, SECONDS));

        k3po.finish();
    }


    @Ignore("#345: WsebConnector does not do close handshake")
    @Test
    @Specification("client.send.close.no.reply.from.server/response")
    public void clientShouldCloseIfServerDoesNotEchoCloseFrame() throws Exception {
        final IoHandler handler = context.mock(IoHandler.class);
        final CountDownLatch closed = new CountDownLatch(1);

        context.checking(new Expectations() {
            {
                oneOf(handler).sessionCreated(with(any(IoSessionEx.class)));
                oneOf(handler).sessionOpened(with(any(IoSessionEx.class)));
                oneOf(handler).sessionClosed(with(any(IoSessionEx.class)));
                will(countDown(closed));
            }
        });

        ConnectFuture connectFuture = connector.connect("ws://localhost:8080/path?query", null, handler);
        assertTrue("Connect failed", connectFuture.await(5,  SECONDS));
        IoSessionEx connectSession = (IoSessionEx) connectFuture.getSession();
        connectSession.close(false);
        assertTrue(closed.await(4, SECONDS));
        k3po.finish();
    }

    // Server only test, not applicable to clients
    //@Test
    //@Specification({
    //    "client.abruptly.closes.downstream/request",
    //    "client.abruptly.closes.downstream/response" })
    //public void clientAbruptlyClosesDownstream() throws Exception {
    //    k3po.finish();
    //}

    //@Ignore("#345: WsebConnector does not do close handshake")
    // When run this test fails with k3po script ComparisonFailure because WsebConnector
    // does not currently do the close handshake (and close happens before the reader(downstream)
    // even gets connected). However the correct events do get fired (sessionClosed, closeFuture).
    @Test
    @Specification("server.send.close/response")
    public void shouldPerformServerInitiatedClose() throws Exception {
        final IoHandler handler = context.mock(IoHandler.class);
        final CountDownLatch closed = new CountDownLatch(1);

        context.checking(new Expectations() {
            {
                oneOf(handler).sessionCreated(with(any(IoSessionEx.class)));
                oneOf(handler).sessionOpened(with(any(IoSessionEx.class)));
                oneOf(handler).sessionClosed(with(any(IoSessionEx.class)));
                will(countDown(closed));
            }
        });

        ConnectFuture connectFuture = connector.connect("ws://localhost:8080/path?query", null, handler);
        IoSessionEx connectSession = (IoSessionEx) connectFuture.getSession();
        CloseFuture closeFuture = connectSession.close(false);
        assertTrue(closed.await(4, SECONDS));
        assertTrue(closeFuture.isClosed());

        k3po.finish();
    }

    // Server only test, not applicable to clients
    //@Test
    //@Specification({
    //    "server.send.close.no.reply.from.client/request",
    //    "server.send.close.no.reply.from.client/response" })
    //public void serverShouldCloseIfClientDoesNotEchoCloseFrame() throws Exception {
    //    k3po.finish();
    //}

    //@Ignore("#345: WsebConnector does not do close handshake")
    @Test
    @Specification("server.send.data.after.close/response")
    public void shouldIgnoreDataFromServerAfterCloseFrame() throws Exception {
        final IoHandler handler = context.mock(IoHandler.class);
        final CountDownLatch closed = new CountDownLatch(1);

        context.checking(new Expectations() {
            {
                oneOf(handler).sessionCreated(with(any(IoSessionEx.class)));
                oneOf(handler).sessionOpened(with(any(IoSessionEx.class)));
                oneOf(handler).sessionClosed(with(any(IoSessionEx.class)));
                will(countDown(closed));
            }
        });

        ConnectFuture connectFuture = connector.connect("ws://localhost:8080/path?query", null, handler);
        IoSessionEx connectSession = (IoSessionEx) connectFuture.getSession();
        CloseFuture closeFuture = connectSession.close(false);
        assertTrue(closed.await(4, SECONDS));
        assertTrue(closeFuture.isClosed());
        k3po.finish();
    }

    //@Ignore("#345: WsebConnector does not do close handshake")
    @Test
    @Specification("server.send.data.after.reconnect/response")
    public void shouldIgnoreDataFromServerAfterReconnectFrame() throws Exception {
        final IoHandler handler = context.mock(IoHandler.class);
        final CountDownLatch closed = new CountDownLatch(1);

        context.checking(new Expectations() {
            {
                oneOf(handler).sessionCreated(with(any(IoSessionEx.class)));
                oneOf(handler).sessionOpened(with(any(IoSessionEx.class)));
                oneOf(handler).sessionClosed(with(any(IoSessionEx.class)));
                will(countDown(closed));
            }
        });

        ConnectFuture connectFuture = connector.connect("ws://localhost:8080/path?query", null, handler);
        IoSessionEx connectSession = (IoSessionEx) connectFuture.getSession();
        CloseFuture closeFuture = connectSession.close(false);
        assertTrue(closed.await(4, SECONDS));
        assertTrue(closeFuture.isClosed());
        k3po.finish();
    }
}
