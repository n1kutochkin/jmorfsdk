
package conversionFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConversionFile {
    
    private static final byte[] CONTROLVALUE = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
    private final BufferedReader inReader;
    private final FileOutputStream outHashCodeAndMorfCharacteristics;
    private final BufferedWriter outHashCodeAndMainFormString;
    private final BufferedWriter outHashCodeAndString;
    private HashSet<String> omoForm;
    
    public ConversionFile(String inPath, String hashCodeAndMorfCharacteristicsPath, String hashCodeAndMainWordStringPath, String hashCodeAndStringPath){
        inReader = openBufferedReaderStreamFromFile(inPath, "Windows-1251");
        outHashCodeAndMorfCharacteristics = openFileInputStreamFromFile(hashCodeAndMorfCharacteristicsPath);
        outHashCodeAndMainFormString = openBufferedWriterStreamFromFile(hashCodeAndMainWordStringPath, "windows-1251");
        outHashCodeAndString = openBufferedWriterStreamFromFile(hashCodeAndStringPath, "windows-1251");
    }
    
    public void conversionFile(){     
        omoForm = new HashSet<>();
        try {
            //Пропускаем первую строчку в которой хранится информация
            inReader.readLine();
            int count = 0;
            while(inReader.ready() && count < 25000) {
                saveLemma(inReader.readLine());
//                count++;
            }
            saveHashCodeAndString();
        } catch (IOException ex) {
            Logger.getLogger(ConversionFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            inReader.close();
            outHashCodeAndMorfCharacteristics.flush();
            outHashCodeAndMainFormString.flush();
            outHashCodeAndString.flush();
        } catch (IOException ex) {
            Logger.getLogger(ConversionFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static BufferedReader openBufferedReaderStreamFromFile(String pathFile, String encoding ) {

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(pathFile), encoding));
        } catch (FileNotFoundException ex) {
            String messages = String.format("Ошибка при чтении файла.\r\nПроверте наличие %s\r\n", pathFile);
            Logger.getLogger(ConversionFile.class.getName()).log(Level.SEVERE, messages);
        } catch (UnsupportedEncodingException ex) {
            String messages = String.format("Ошибка при чтении файла.\r\n1)Проверте кодировку %s в соотвевствии с параметрами в property.xml.\r\n2)При отсутствии property.xml кодировка по умолчанию %s\r\n\r\n",
                pathFile, encoding);
            Logger.getLogger(ConversionFile.class.getName()).log(Level.SEVERE, messages);
        }

        return bufferedReader;
    }
    
    private static FileOutputStream openFileInputStreamFromFile(String pathFile ) {
        
        FileOutputStream fileInputStream = null;
        try {
            fileInputStream = new FileOutputStream(pathFile);
        } catch (FileNotFoundException ex) {
            String messages = String.format("Ошибка при чтении файла.\r\nПроверте наличие %s\r\n", pathFile);
            Logger.getLogger(ConversionFile.class.getName()).log(Level.SEVERE, messages);
        }

        return fileInputStream;
    }
    private static BufferedWriter openBufferedWriterStreamFromFile(String pathFile, String encoding ) {

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathFile), encoding));
        } catch (FileNotFoundException ex) {
            String messages = String.format("Ошибка при чтении файла.\r\nПроверте наличие %s\r\n", pathFile);
            Logger.getLogger(ConversionFile.class.getName()).log(Level.SEVERE, messages);
        } catch (UnsupportedEncodingException ex) {
            String messages = String.format("Ошибка при чтении файла.\r\n1)Проверте кодировку %s в соотвевствии с параметрами в property.xml.\r\n2)При отсутствии property.xml кодировка по умолчанию %s\r\n\r\n",
                pathFile, encoding);
            Logger.getLogger(ConversionFile.class.getName()).log(Level.SEVERE, messages);
        }

        return bufferedWriter;
    }
    
    private void saveLemma(String strForms) {
        saveMainForm(strForms);
        saveWordForms(strForms);
        saveEndLemma();
    }
    
    private void saveMainForm(String strForms) {
        String mainWordForm;
        if (strForms.contains("\"")) {
            mainWordForm = strForms.substring(0, strForms.indexOf("\""));
        } else {
            mainWordForm = strForms;
        }
        String[] mainWordParameters = mainWordForm.split(" ");
        
        try {
            int hashCodeForm = mainWordParameters[0].hashCode();
            outHashCodeAndMorfCharacteristics.write(getBytes(hashCodeForm));
            outHashCodeAndMorfCharacteristics.write(Byte.decode("0x" + mainWordParameters[1]));
            outHashCodeAndMorfCharacteristics.write(getBytes(new BigInteger(mainWordParameters[2], 16).longValue()));
            
            outHashCodeAndMainFormString.write(hashCodeToStringHer(hashCodeForm));
            outHashCodeAndMainFormString.write(mainWordParameters[0]);            
            outHashCodeAndMainFormString.newLine();
        } catch (IOException ex) {
            Logger.getLogger(ConversionFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static String hashCodeToStringHer(int hashCode) {
        String hashCodeString = Integer.toHexString(hashCode);
        while(hashCodeString.length() < 8) {
            hashCodeString = "0" + hashCodeString;
        }
        return hashCodeString;
    }
        
    private static byte [] getBytes(int value) {
        byte[] bytes = new byte[]{
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) (value)
        };
        return bytes;
    }
    
    private static byte[] getBytes(long value) {
        byte[] bytes = new byte[]{
            (byte) (value >> 56),
            (byte) (value >> 48),
            (byte) (value >> 40),
            (byte) (value >> 32),
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) (value)
        };
        return bytes;
    }
    
    private void saveWordForms(String strLemma) {

        String[] arrayWordForms = strLemma.split("\"");

        for (int i = 1; i < arrayWordForms.length; i++) {
            saveWordForm(arrayWordForms[i]);
        }
    }
    
    private void saveWordForm(String strForm) {
        
        String[] mainWordParam = strForm.split(" ");
        try {
            int hashCodeForm = mainWordParam[0].hashCode();
            if(hashCodeForm == 0) {
                System.err.println("");
            }
            outHashCodeAndMorfCharacteristics.write(getBytes(hashCodeForm));
            outHashCodeAndMorfCharacteristics.write(getBytes(new BigInteger(mainWordParam[1], 16).longValue()));
            
            omoForm.add(mainWordParam[0]);
        } catch (IOException ex) {
            Logger.getLogger(ConversionFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveEndLemma(){
        try {
            outHashCodeAndMorfCharacteristics.write(CONTROLVALUE);
        } catch (IOException ex) {
            Logger.getLogger(ConversionFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveHashCodeAndString(){
        for(Object obj : omoForm.toArray()) {
            String str = (String) obj;
            try {
                outHashCodeAndString.write(hashCodeToStringHer(str.hashCode()));
                outHashCodeAndString.write(str); 
                outHashCodeAndString.newLine();
            } catch (IOException ex) {
                Logger.getLogger(ConversionFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void main(String[] args) {
        String inPath = "dictionary.format.number.txt";
        String hashCodeAndMorfCharacteristicsPath = "dictionary.format.hash+morfCharacteristic.txt";
        String hashCodeAndMainFormStringPath = "dictionary.format.hash+mainFormString.txt";
        String hashCodeAndStringPath = "dictionary.format.hash+wordFormString.txt";
        
        ConversionFile converFile = new ConversionFile(inPath, hashCodeAndMorfCharacteristicsPath, hashCodeAndMainFormStringPath, hashCodeAndStringPath);
        converFile.conversionFile();
    }
    
}
