package command;

import geometry.batch.Batched3DModel;
import geometry.types.FormatType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileExistsException;
import org.apache.logging.log4j.Level;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import tiler.BatchInfo;
import tiler.Gaia3DTiler;
import tiler.Tiler;
import tiler.tileset.Tileset;

import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.List;


@Slf4j
public class TilerMain {
    public static Options createOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "Print help");
        options.addOption("v", "version", false, "Print version");
        options.addOption("q", "quiet", false, "Quiet mode");

        options.addOption("i", "input", true, "Input file path");
        options.addOption("o", "output", true, "Output file path");
        options.addOption("it", "inputType", true, "Input file type");
        options.addOption("ot", "outputType", true, "Output file type");

        options.addOption("s", "srs", true, "Spatial Reference Systems EPSG code");
        options.addOption("r", "recursive", false, "Recursive search directory");
        options.addOption("sc", "scale", true, "Scale factor");
        options.addOption("st", "strict", true, "Strict mode");

        options.addOption("gn", "genNormals", false, "generate normals");
        options.addOption("nt", "ignoreTextures", false, "ignore textures");
        options.addOption("yz", "swapYZ", false, "swap YZ");

        options.addOption("d", "debug", false, "Debug mode");
        options.addOption("gf", "gltf", false, "Create gltf file");
        options.addOption("gb", "glb", false, "Create glb file");
        return options;
    }

    public static void main(String[] args) {
        Configurator.initLogger();
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("quiet")) {
                Configurator.setLevel(Level.OFF);
            }
            start();
            if (cmd.hasOption("debug")) {
                log.info("Starting Gaia3D Tiler in debug mode.");
            }
            if (cmd.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Gaia3D Tiler", options);
                return;
            }
            if (cmd.hasOption("version")) {
                log.info("Gaia3D Tiler version 1.0.0");
                return;
            }
            if (!cmd.hasOption("input")) {
                log.error("input file path is not specified.");
                return;
            }
            if (!cmd.hasOption("output")) {
                log.error("output file path is not specified.");
                return;
            }
            if (!cmd.hasOption("srs")) {
                log.error("srs.");
                return;
            }
            File inputFile = new File(cmd.getOptionValue("input"));
            File outputFile = new File(cmd.getOptionValue("output"));
            excute(cmd, inputFile, outputFile);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            log.error("Failed to parse command line properties", e);
        }
        underline();
    }

    private static void excute(CommandLine command, File inputFile, File outputFile) throws IOException {
        long start = System.currentTimeMillis();

        String srs = command.getOptionValue("srs");
        String inputExtension = command.getOptionValue("inputType");
        Path inputPath = inputFile.toPath();
        Path outputPath = outputFile.toPath();
        inputFile.mkdir();
        outputFile.mkdir();
        if (!validate(outputPath)) {
            return;
        }
        FormatType formatType = FormatType.fromExtension(inputExtension);
        CRSFactory factory = new CRSFactory();
        CoordinateReferenceSystem source = factory.createFromName("EPSG:" + srs);



        Tiler tiler = new Gaia3DTiler(inputPath, outputPath, formatType, source, command);
        Tileset tileset = tiler.tile();
        List<BatchInfo> batchInfos = tileset.findAllBatchInfo();

        batchInfos.forEach(batchInfo -> {
            Batched3DModel batched3DModel = new Batched3DModel(batchInfo, command);
            try {
                batched3DModel.write();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        long end = System.currentTimeMillis();
        log.info("Tiling finished in {} seconds.", (end - start) / 1000);
    }

    private static boolean validate(Path outputPath) throws IOException {
        File output = outputPath.toFile();
        if (!output.exists()) {
            throw new FileExistsException("output path is not exist.");
        } else if (!output.isDirectory()) {
            throw new NotDirectoryException("output path is not directory.");
        } else if (!output.canWrite()) {
            throw new IOException("output path is not writable.");
        }
        return true;
    }

    private static void start() {
        log.info(
            " _______  ___      _______  _______  __   __  _______ \n" +
            "|       ||   |    |   _   ||       ||  |_|  ||   _   |\n" +
            "|    _  ||   |    |  |_|  ||  _____||       ||  |_|  |\n" +
            "|   |_| ||   |    |       || |_____ |       ||       |\n" +
            "|    ___||   |___ |       ||_____  ||       ||       |\n" +
            "|   |    |       ||   _   | _____| || ||_|| ||   _   |\n" +
            "|___|    |_______||__| |__||_______||_|   |_||__| |__|");
        underline();
    }

    private static void underline() {
        log.info("======================================================");
    }
}
