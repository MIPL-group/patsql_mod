/**
 *  (Foresighter Mod)
 *  CEGIS Tests
 */

package patsql.synth.benchmark;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import patsql.synth.Debug;
import patsql.synth.cegis.SUtil;


import java.io.File;

class CEGISTest {

	@Test
	@Tag("CEGIS")
	void testCEGIS(){
		Debug.isDebugMode = false;
//        CEGISLogRecorder log = new CEGISLogRecorder("AllBenchmarks", "0921", "CUBES_spider", "product_catalog", "0016");
//        SUtil.synthesizeFromScytheFileCEGIS(new File("BenchmarksCheck/AllBenchmarks/spider_md/activity_1/0001_3080.md"), 100000, "test_cegis", "debug");
		SUtil.synthesizeFromScytheFileCEGIS(new File("Benchmarks/test/0001_3081.md"), 1000, "test_cegis", "debug");
	}


}
