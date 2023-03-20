package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.installer.NgrokVersion;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Tunnel;

import br.pro.hashi.sdx.rest.server.exception.ServerException;

class RestServerTest {
	private Server jettyServer;
	private RestServer s;
	private InetAddress local;
	private InetAddress loopback;
	private InetAddress ngrok;
	private JavaNgrokConfig config;
	private NgrokClient client;
	private CreateTunnel create;
	private Tunnel tunnel;
	private MockedStatic<InetAddress> inetAddress;
	private MockedConstruction<JavaNgrokConfig.Builder> configConstruction;
	private MockedConstruction<NgrokClient.Builder> clientConstruction;
	private MockedConstruction<CreateTunnel.Builder> createConstruction;

	@BeforeEach
	void setUp() {
		jettyServer = mock(Server.class);
		local = mock(InetAddress.class);
		when(local.getHostAddress()).thenReturn("123.456.789.101");
		when(local.getHostName()).thenReturn("local");
		loopback = mock(InetAddress.class);
		when(loopback.getHostAddress()).thenReturn("112.131.415.161");
		when(loopback.getHostName()).thenReturn("loopback");
		ngrok = mock(InetAddress.class);
		config = mock(JavaNgrokConfig.class);
		client = mock(NgrokClient.class);
		create = mock(CreateTunnel.class);
		tunnel = mock(Tunnel.class);
		when(client.connect(create)).thenReturn(tunnel);
		inetAddress = mockStatic(InetAddress.class);
		inetAddress.when(() -> InetAddress.getLoopbackAddress()).thenReturn(loopback);
		configConstruction = mockConstruction(JavaNgrokConfig.Builder.class, (mock, context) -> {
			when(mock.withNgrokVersion(NgrokVersion.V3)).thenReturn(mock);
			when(mock.build()).thenReturn(config);
		});
		clientConstruction = mockConstruction(NgrokClient.Builder.class, (mock, context) -> {
			when(mock.withJavaNgrokConfig(config)).thenReturn(mock);
			when(mock.build()).thenReturn(client);
		});
		createConstruction = mockConstruction(CreateTunnel.Builder.class, (mock, context) -> {
			when(mock.withBindTls(any(boolean.class))).thenReturn(mock);
			when(mock.withAddr(any(int.class))).thenReturn(mock);
			when(mock.build()).thenReturn(create);
		});
	}

	@AfterEach
	void tearDown() {
		createConstruction.close();
		clientConstruction.close();
		configConstruction.close();
		inetAddress.close();
	}

	@Test
	void builds() {
		try (MockedConstruction<RestServerBuilder> construction = mockBuilderConstruction()) {
			assertSame(s, RestServer.from("package"));
			RestServerBuilder builder = construction.constructed().get(0);
			verify(builder).build("package");
		}
	}

	private MockedConstruction<RestServerBuilder> mockBuilderConstruction() {
		s = mock(RestServer.class);
		return mockConstruction(RestServerBuilder.class, (mock, context) -> {
			when(mock.build("package")).thenReturn(s);
		});
	}

	@Test
	void starts() throws Exception {
		s = newRestServer();
		mockLocalHost();
		when(jettyServer.isRunning()).thenReturn(false);
		s.start();
		verify(jettyServer).start();
		assertEquals("123.456.789.101", s.getPublicAddress());
		assertEquals("https://local:8443", s.getPublicUrl());
		assertEquals("https://local:8843", s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("https://loopback:8443", s.getPrivateUrl());
		assertEquals("https://loopback:8843", s.getPrivateUrl3());
	}

	@Test
	void startsWithDefaultPort() throws Exception {
		s = newRestServerWithDefaultPort();
		mockLocalHost();
		when(jettyServer.isRunning()).thenReturn(false);
		s.start();
		verify(jettyServer).start();
		assertEquals("123.456.789.101", s.getPublicAddress());
		assertEquals("https://local", s.getPublicUrl());
		assertEquals("https://local:8843", s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("https://loopback", s.getPrivateUrl());
		assertEquals("https://loopback:8843", s.getPrivateUrl3());
	}

	@Test
	void startsWithoutHttps3() throws Exception {
		s = newRestServerWithoutHttps3();
		mockLocalHost();
		when(jettyServer.isRunning()).thenReturn(false);
		s.start();
		verify(jettyServer).start();
		assertEquals("123.456.789.101", s.getPublicAddress());
		assertEquals("https://local:8443", s.getPublicUrl());
		assertNull(s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("https://loopback:8443", s.getPrivateUrl());
		assertNull(s.getPrivateUrl3());
	}

	@Test
	void startsWithoutHttps() throws Exception {
		s = newRestServerWithoutHttps();
		mockLocalHost();
		when(jettyServer.isRunning()).thenReturn(false);
		s.start();
		verify(jettyServer).start();
		assertEquals("123.456.789.101", s.getPublicAddress());
		assertEquals("http://local:8080", s.getPublicUrl());
		assertNull(s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("http://loopback:8080", s.getPrivateUrl());
		assertNull(s.getPrivateUrl3());
	}

	@Test
	void startsWihtoutHttpsWithDefaultPort() throws Exception {
		s = newRestServerWithoutHttpsWithDefaultPort();
		mockLocalHost();
		when(jettyServer.isRunning()).thenReturn(false);
		s.start();
		verify(jettyServer).start();
		assertEquals("123.456.789.101", s.getPublicAddress());
		assertEquals("http://local", s.getPublicUrl());
		assertNull(s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("http://loopback", s.getPrivateUrl());
		assertNull(s.getPrivateUrl3());
	}

	@Test
	void startsWithoutLocal() throws Exception {
		s = newRestServer();
		inetAddress.when(() -> InetAddress.getLocalHost()).thenThrow(UnknownHostException.class);
		when(jettyServer.isRunning()).thenReturn(false);
		s.start();
		verify(jettyServer).start();
		assertNull(s.getPublicAddress());
		assertNull(s.getPublicUrl());
		assertNull(s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("https://loopback:8443", s.getPrivateUrl());
		assertEquals("https://loopback:8843", s.getPrivateUrl3());
	}

	@Test
	void startsWithTunnel() throws Exception {
		s = newRestServer();
		mockTunnelUrlAndNgrokAddress();
		when(jettyServer.isRunning()).thenReturn(false);
		s.startWithTunnel();
		verify(jettyServer).start();
		assertEquals("617.181.920.212", s.getPublicAddress());
		assertEquals("https://ngroks", s.getPublicUrl());
		assertNull(s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("https://loopback:8443", s.getPrivateUrl());
		assertEquals("https://loopback:8843", s.getPrivateUrl3());
	}

	@Test
	void startsWithTunnelWithDefaultPort() throws Exception {
		s = newRestServerWithDefaultPort();
		mockTunnelUrlAndNgrokAddress();
		when(jettyServer.isRunning()).thenReturn(false);
		s.startWithTunnel();
		verify(jettyServer).start();
		assertEquals("617.181.920.212", s.getPublicAddress());
		assertEquals("https://ngroks", s.getPublicUrl());
		assertNull(s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("https://loopback", s.getPrivateUrl());
		assertEquals("https://loopback:8843", s.getPrivateUrl3());
	}

	@Test
	void startsWithTunnelWithoutHttps3() throws Exception {
		s = newRestServerWithoutHttps3();
		mockTunnelUrlAndNgrokAddress();
		when(jettyServer.isRunning()).thenReturn(false);
		s.startWithTunnel();
		verify(jettyServer).start();
		assertEquals("617.181.920.212", s.getPublicAddress());
		assertEquals("https://ngroks", s.getPublicUrl());
		assertNull(s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("https://loopback:8443", s.getPrivateUrl());
		assertNull(s.getPrivateUrl3());
	}

	private void mockTunnelUrlAndNgrokAddress() {
		mockTunnelUrl();
		mockNgrokAddress("ngroks", "617.181.920.212");
	}

	@Test
	void startsWithTunnelWithoutHttps() throws Exception {
		s = newRestServerWithoutHttps();
		mockTunnelUrlAndNgrokAddressWithoutHttps();
		when(jettyServer.isRunning()).thenReturn(false);
		s.startWithTunnel();
		verify(jettyServer).start();
		assertEquals("223.242.526.272", s.getPublicAddress());
		assertEquals("http://ngrok", s.getPublicUrl());
		assertNull(s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("http://loopback:8080", s.getPrivateUrl());
		assertNull(s.getPrivateUrl3());
	}

	@Test
	void startsWithTunnelWithoutHttpsWithDefaultPort() throws Exception {
		s = newRestServerWithoutHttpsWithDefaultPort();
		mockTunnelUrlAndNgrokAddressWithoutHttps();
		when(jettyServer.isRunning()).thenReturn(false);
		s.startWithTunnel();
		verify(jettyServer).start();
		assertEquals("223.242.526.272", s.getPublicAddress());
		assertEquals("http://ngrok", s.getPublicUrl());
		assertNull(s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("http://loopback", s.getPrivateUrl());
		assertNull(s.getPrivateUrl3());
	}

	private void mockTunnelUrlAndNgrokAddressWithoutHttps() {
		mockTunnelUrl("http://ngrok");
		mockNgrokAddress("ngrok", "223.242.526.272");
	}

	private void mockNgrokAddress(String authority, String address) {
		inetAddress.when(() -> InetAddress.getByName(authority)).thenReturn(ngrok);
		when(ngrok.getHostAddress()).thenReturn(address);
	}

	@Test
	void startsWithTunnelWithoutAddress() throws Exception {
		s = newRestServer();
		mockTunnelUrl();
		inetAddress.when(() -> InetAddress.getByName("ngroks")).thenThrow(UnknownHostException.class);
		when(jettyServer.isRunning()).thenReturn(false);
		s.startWithTunnel();
		verify(jettyServer).start();
		assertNull(s.getPublicAddress());
		assertEquals("https://ngroks", s.getPublicUrl());
		assertNull(s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("https://loopback:8443", s.getPrivateUrl());
		assertEquals("https://loopback:8843", s.getPrivateUrl3());
	}

	private void mockTunnelUrl() {
		mockTunnelUrl("https://ngroks");
	}

	private void mockTunnelUrl(String url) {
		when(tunnel.getPublicUrl()).thenReturn(url);
	}

	@Test
	void doesNotStartIfJettyServerAlreadyStarted() throws Exception {
		s = newRestServer();
		mockLocalHost();
		when(jettyServer.isRunning()).thenReturn(false);
		s.start();
		when(jettyServer.isRunning()).thenReturn(true);
		s.start();
		verify(jettyServer, times(1)).start();
		assertEquals("123.456.789.101", s.getPublicAddress());
		assertEquals("https://local:8443", s.getPublicUrl());
		assertEquals("https://local:8843", s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("https://loopback:8443", s.getPrivateUrl());
		assertEquals("https://loopback:8843", s.getPrivateUrl3());
	}

	@Test
	void doesNotStartIfJettyServerThrowsException() throws Exception {
		s = newRestServer();
		mockLocalHost();
		when(jettyServer.isRunning()).thenReturn(false);
		Throwable cause = new Exception();
		doThrow(cause).when(jettyServer).start();
		Exception exception = assertThrows(ServerException.class, () -> {
			s.start();
		});
		assertSame(cause, exception.getCause());
		assertNull(s.getPublicAddress());
		assertNull(s.getPublicUrl());
		assertNull(s.getPublicUrl3());
		assertNull(s.getPrivateAddress());
		assertNull(s.getPrivateUrl());
		assertNull(s.getPrivateUrl3());
	}

	@Test
	void stops() throws Exception {
		s = newRestServer();
		mockLocalHost();
		when(jettyServer.isRunning()).thenReturn(false);
		s.start();
		when(jettyServer.isRunning()).thenReturn(true);
		s.stop();
		verify(jettyServer).stop();
		assertNull(s.getPublicAddress());
		assertNull(s.getPublicUrl());
		assertNull(s.getPublicUrl3());
		assertNull(s.getPrivateAddress());
		assertNull(s.getPrivateUrl());
		assertNull(s.getPrivateUrl3());
	}

	@Test
	void doesNotStopIfJettyServerAlreadyStopped() throws Exception {
		s = newRestServer();
		mockLocalHost();
		when(jettyServer.isRunning()).thenReturn(false);
		s.stop();
		verify(jettyServer, times(0)).stop();
		assertNull(s.getPublicAddress());
		assertNull(s.getPublicUrl());
		assertNull(s.getPublicUrl3());
		assertNull(s.getPrivateAddress());
		assertNull(s.getPrivateUrl());
		assertNull(s.getPrivateUrl3());
	}

	@Test
	void doesNotStopIfJettyServerThrowsException() throws Exception {
		s = newRestServer();
		mockLocalHost();
		when(jettyServer.isRunning()).thenReturn(false);
		s.start();
		when(jettyServer.isRunning()).thenReturn(true);
		Throwable cause = new Exception();
		doThrow(cause).when(jettyServer).stop();
		Exception exception = assertThrows(ServerException.class, () -> {
			s.stop();
		});
		assertSame(cause, exception.getCause());
		assertEquals("123.456.789.101", s.getPublicAddress());
		assertEquals("https://local:8443", s.getPublicUrl());
		assertEquals("https://local:8843", s.getPublicUrl3());
		assertEquals("112.131.415.161", s.getPrivateAddress());
		assertEquals("https://loopback:8443", s.getPrivateUrl());
		assertEquals("https://loopback:8843", s.getPrivateUrl3());
	}

	private void mockLocalHost() {
		inetAddress.when(() -> InetAddress.getLocalHost()).thenReturn(local);
	}

	private RestServer newRestServer() {
		return newRestServer(8443, 8843);
	}

	private RestServer newRestServerWithDefaultPort() {
		return newRestServer(443, 8843);
	}

	private RestServer newRestServerWithoutHttps3() {
		return newRestServer(8443, -1);
	}

	private RestServer newRestServer(int mainPort, int altPort) {
		return newRestServer("https", 8080, mainPort, altPort);
	}

	private RestServer newRestServerWithoutHttps() {
		return newRestServerWithoutHttps(8080);
	}

	private RestServer newRestServerWithoutHttpsWithDefaultPort() {
		return newRestServerWithoutHttps(80);
	}

	private RestServer newRestServerWithoutHttps(int clearPort) {
		return newRestServer("http", clearPort, clearPort, -1);
	}

	private RestServer newRestServer(String scheme, int clearPort, int mainPort, int altPort) {
		return new RestServer(jettyServer, scheme, clearPort, mainPort, altPort);
	}
}
