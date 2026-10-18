package io.github.jqssun.displaymirror;
import org.junit.Test;
import static org.junit.Assert.*;
public class AirLinkTvRemoteClientTest {
 @Test public void acceptsPrivateNetworks(){
  assertTrue(AirLinkTvRemoteClient.localIpv4("192.168.1.22"));
  assertTrue(AirLinkTvRemoteClient.localIpv4("10.0.2.15"));
  assertTrue(AirLinkTvRemoteClient.localIpv4("172.16.0.5"));
  assertTrue(AirLinkTvRemoteClient.localIpv4("172.31.255.1"));
 }
 @Test public void blocksPublicAndMalformedHosts(){
  assertFalse(AirLinkTvRemoteClient.localIpv4("8.8.8.8"));
  assertFalse(AirLinkTvRemoteClient.localIpv4("example.com"));
  assertFalse(AirLinkTvRemoteClient.localIpv4("192.168.1.500"));
  assertFalse(AirLinkTvRemoteClient.localIpv4("192.168.1.1:8060"));
  assertFalse(AirLinkTvRemoteClient.localIpv4("172.32.1.1"));
  assertFalse(AirLinkTvRemoteClient.localIpv4("127.0.0.1"));
 }
}