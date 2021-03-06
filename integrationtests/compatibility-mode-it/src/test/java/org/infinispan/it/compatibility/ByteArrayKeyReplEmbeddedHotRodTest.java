package org.infinispan.it.compatibility;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.VersionedValue;
import org.infinispan.commons.equivalence.ByteArrayEquivalence;
import org.infinispan.configuration.cache.CacheMode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Tests embedded and Hot Rod compatibility in a replicated clustered environment using byte array keys.
 *
 * @author Martin Gencur
 * @author Pedro Ruivo
 * @since 6.0
 */
@Test(groups = "functional", testName = "it.compatibility.ByteArrayKeyReplEmbeddedHotRodTest")
public class ByteArrayKeyReplEmbeddedHotRodTest {

   CompatibilityCacheFactory<Object, Object> cacheFactory1;
   CompatibilityCacheFactory<Object, Object> cacheFactory2;

   public void testHotRodPutEmbeddedGet() throws Exception {
      final byte[] key = "4".getBytes();
      final String value = "v1";

      // 1. Put with HotRod
      RemoteCache<Object, Object> remote = cacheFactory1.getHotRodCache();
      assertNull(remote.withFlags(Flag.FORCE_RETURN_VALUE).put(key, value));

      // 2. Get with Embedded
      assertEquals(value, cacheFactory2.getEmbeddedCache().get(key));
   }

   public void testHotRodReplace() throws Exception {
      final byte[] key = "5".getBytes();
      final String value1 = "v1";
      final String value2 = "v2";

      // 1. Put with HotRod
      RemoteCache<Object, Object> remote = cacheFactory1.getHotRodCache();
      assertNull(remote.withFlags(Flag.FORCE_RETURN_VALUE).put(key, value1));

      // 2. Replace with HotRod
      VersionedValue versioned = cacheFactory1.getHotRodCache().getVersioned(key);
      assertTrue(cacheFactory1.getHotRodCache().replaceWithVersion(key, value2, versioned.getVersion()));
   }

   public void testHotRodRemove() throws Exception {
      final byte[] key = "7".getBytes();
      final String value = "v1";

      // 1. Put with HotRod
      RemoteCache<Object, Object> remote = cacheFactory1.getHotRodCache();
      assertNull(remote.withFlags(Flag.FORCE_RETURN_VALUE).put(key, value));

      // 2. Removed with HotRod
      VersionedValue versioned = cacheFactory1.getHotRodCache().getVersioned(key);
      assertTrue(cacheFactory1.getHotRodCache().removeWithVersion(key, versioned.getVersion()));
   }

   //This test can fail only if there's a marshaller specified for EmbeddedTypeConverter
   public void testEmbeddedPutHotRodGet() throws Exception {
      final byte[] key = "8".getBytes();
      final String value = "v1";

      // 1. Put with Embedded
      assertNull(cacheFactory2.getEmbeddedCache().put(key, value));

      // 2. Get with HotRod
      assertEquals(value, cacheFactory1.getHotRodCache().get(key));
   }

   @BeforeClass
   protected void setup() throws Exception {
      cacheFactory1 = new CompatibilityCacheFactory<Object, Object>(CacheMode.REPL_SYNC)
            .keyEquivalence(ByteArrayEquivalence.INSTANCE).setup();
      cacheFactory2 = new CompatibilityCacheFactory<Object, Object>(CacheMode.REPL_SYNC)
            .keyEquivalence(ByteArrayEquivalence.INSTANCE)
            .setup(cacheFactory1.getHotRodPort(), 100);
   }

   @AfterClass
   protected void teardown() {
      CompatibilityCacheFactory.killCacheFactories(cacheFactory1, cacheFactory2);
   }
}
