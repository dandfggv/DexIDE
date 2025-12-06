package dexide;

import dexlang.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DexIDE {

    private JFrame frame;
    private JTextArea editor;
    private JTextArea console;
    private File currentFile;
    private File projectRoot;

    public DexIDE() {
        frame = new JFrame("Dex IDE - Beast Edition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 750);

        editor = new JTextArea();
        editor.setFont(new Font("Consolas", Font.PLAIN, 14));

        console = new JTextArea();
        console.setEditable(false);
        console.setFont(new Font("Consolas", Font.PLAIN, 12));
        console.setBackground(Color.BLACK);
        console.setForeground(new Color(0, 255, 0));
        console.setLineWrap(true);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(editor),
                new JScrollPane(console));
        split.setDividerLocation(480);
        split.setResizeWeight(0.7);

        frame.setJMenuBar(createMenuBar());
        frame.add(split, BorderLayout.CENTER);

        editor.setText(sampleCode());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open...");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        JMenuItem exitItem = new JMenuItem("Exit");

        newItem.addActionListener(e -> {
            editor.setText("");
            console.setText("");
            currentFile = null;
        });

        openItem.addActionListener(this::openFile);
        saveItem.addActionListener(e -> saveFile(false));
        saveAsItem.addActionListener(e -> saveFile(true));
        exitItem.addActionListener(e -> frame.dispose());

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu projectMenu = new JMenu("Project");
        JMenuItem chooseRootItem = new JMenuItem("Choose Project Root...");
        chooseRootItem.addActionListener(e -> chooseProjectRoot());
        projectMenu.add(chooseRootItem);

        JMenu buildMenu = new JMenu("Build / Run");
        JMenuItem runItem = new JMenuItem("Run Dex");
        JMenuItem buildItem = new JMenuItem("Build Project");

        runItem.addActionListener(e -> runDex());
        buildItem.addActionListener(e -> buildProject());

        buildMenu.add(runItem);
        buildMenu.add(buildItem);

        bar.add(fileMenu);
        bar.add(projectMenu);
        bar.add(buildMenu);

        return bar;
    }

    private void openFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Dex Files (*.dex)", "dex"));
        int res = chooser.showOpenDialog(frame);
        if (res == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            try {
                String text = Files.readString(currentFile.toPath());
                editor.setText(text);
                console.setText("");
            } catch (Exception ex) {
                console.setText("Error opening file:\n" + ex.getMessage());
            }
        }
    }

    private void saveFile(boolean saveAs) {
        try {
            if (saveAs || currentFile == null) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("Dex Files (*.dex)", "dex"));
                int res = chooser.showSaveDialog(frame);
                if (res != JFileChooser.APPROVE_OPTION) return;
                currentFile = chooser.getSelectedFile();
                if (!currentFile.getName().toLowerCase().endsWith(".dex")) {
                    currentFile = new File(currentFile.getParentFile(), currentFile.getName() + ".dex");
                }
            }
            Files.writeString(currentFile.toPath(), editor.getText());
        } catch (Exception ex) {
            console.setText("Error saving file:\n" + ex.getMessage());
        }
    }

    private void chooseProjectRoot() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose Project Root Folder");
        int res = chooser.showOpenDialog(frame);
        if (res == JFileChooser.APPROVE_OPTION) {
            projectRoot = chooser.getSelectedFile();
            console.setText("Project root set to:\n" + projectRoot.getAbsolutePath());
        }
    }

    private void runDex() {
        console.setText("");
        String source = editor.getText();
        try {
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.lex();
            Parser parser = new Parser(tokens);
            Ast.Block program = parser.parse();
            Interpreter interpreter = new Interpreter();
            String output = interpreter.interpret(program);
            console.setText(output);
        } catch (Exception ex) {
            console.setText("Error while running Dex:\n" + ex.getMessage());
        }
    }

    private void buildProject() {
        console.setText("");

        if (projectRoot == null) {
            console.setText("No project root set.\nChoose Project → Choose Project Root first.");
            return;
        }

        String source = editor.getText();
        if (source.trim().isEmpty()) {
            console.setText("Nothing to build. The editor is empty.");
            return;
        }

        File outputDir = new File(projectRoot, ".dex-output");
        File dumpDir = new File(outputDir, ".dexDump");

        outputDir.mkdirs();
        dumpDir.mkdirs();

        File mainDexFile = new File(outputDir, "program.dex");
        File lastBuildLog = new File(outputDir, "lastBuild.log");
        File lastRunOutput = new File(outputDir, "lastRun.txt");

        StringBuilder buildLog = new StringBuilder();
        String runOutput = "";
        boolean success = false;

        try {
            Files.writeString(mainDexFile.toPath(), source);
            buildLog.append("Saved program.dex\n");
        } catch (IOException e) {
            console.setText("Error saving program.dex:\n" + e.getMessage());
            return;
        }

        try {
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.lex();
            Parser parser = new Parser(tokens);
            Ast.Block program = parser.parse();
            Interpreter interpreter = new Interpreter();
            runOutput = interpreter.interpret(program);
            success = true;
            buildLog.append("Build succeeded.\n");
        } catch (Exception ex) {
            success = false;
            buildLog.append("Build failed:\n").append(ex.getMessage()).append("\n");
            runOutput = "Error:\n" + ex.getMessage();
        }

        try {
            Files.writeString(lastBuildLog.toPath(), buildLog.toString());
            Files.writeString(lastRunOutput.toPath(), runOutput);
        } catch (IOException e) {
            console.setText("Error writing logs:\n" + e.getMessage());
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        File dumpFile = new File(dumpDir, "build-" + timestamp + ".log");

        String dumpContent =
                "Timestamp: " + timestamp + "\n\n" +
                "=== SOURCE ===\n" + source + "\n\n" +
                "=== BUILD LOG ===\n" + buildLog + "\n" +
                "=== RUN OUTPUT ===\n" + runOutput + "\n";

        try {
            Files.writeString(dumpFile.toPath(), dumpContent);
        } catch (IOException e) {
            console.setText("Build done, but dump file failed:\n" + e.getMessage());
            return;
        }

        if (success) {
            console.setText("Build SUCCESS.\n\nOutput:\n" + runOutput +
                    "\n\nFiles created in:\n" + outputDir.getAbsolutePath());
        } else {
            console.setText("Build FAILED.\n\nSee logs in:\n" +
                    outputDir.getAbsolutePath() +
                    "\n\nError:\n" + runOutput);
        }
    }

    private String sampleCode() {
        return """
               int x = 5;
               int y = 10;
               int z = x + y;
               print(z);

               while (z < 50) {
                   z = z + 5;
                   print(z);
               }

               if (z == 50) {
                   print(999);
               }
               """;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DexIDE::new);
    }
}
