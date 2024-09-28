/**
 * (Foresighter Mod)
 *  Helper for Runner CEGIS for experiments and automatically write CEGIS logs.
 */


package patsql.synth.cegis;

import java.io.File;
import patsql.entity.synth.Example;
import patsql.entity.synth.SynthOption;
import patsql.entity.table.Cell;
import patsql.ra.operator.RAOperator;
import patsql.ra.util.Utils;
import patsql.ra.util.ScytheFileDataWithID;
import patsql.synth.RASynthesizer;
import patsql.ra.util.ScytheFileData;

import patsql.synth.CEGISLogRecorder;

public class SUtil {

    /**
     * (Foresighter Mod)
     * Synthesize a problem for our Benchmark Check in CEGIS and record that into a corresponding Log Recorder
     * @param file
     * @param timeoutMs
     * @param logBaseDir
     */
    public static void synthesizeFromScytheFileCEGIS(File file, int timeoutMs, String logBaseDir, String trial) {
        ScytheFileDataWithID sfdID = null;
        ScytheFileData sfd = null;

        try{
            sfdID = Utils.loadFromScytheFileWithID(file);
            sfd = sfdID.getFileData();
        }catch(Throwable e){
            if (e instanceof OutOfMemoryError) {
                System.err.println("Error: out of memory while reading file: " + file.getName());
                return;
            } else {
                throw e;
            }
        }

        String source = sfdID.getSource();
        String group = sfdID.getGroup();
        String id = sfdID.getId();

        Example ex = new Example(sfd.getOutput(), sfd.getInputsAsList());
        SynthOption opt = new SynthOption(sfd.getConstVals().toArray(new Cell[0]));
        RASynthesizer synth = new RASynthesizer(ex, opt);
        CEGISLogRecorder logger = new CEGISLogRecorder(logBaseDir, trial, source, group, id);

        try{
            synth.synthesizeCEGIS(logger, timeoutMs);
        }catch(Throwable e){
            if (e instanceof OutOfMemoryError) {
                logger.writeCSV();
                System.err.println("Error: out of memory while processing file: " + file.getName());
            } else {
                throw e;
            }
        }
        // No need to return or print anything, all results are logged by the CEGISLogRecorder
    }


}