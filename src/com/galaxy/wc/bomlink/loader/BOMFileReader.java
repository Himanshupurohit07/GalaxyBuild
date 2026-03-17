package com.galaxy.wc.bomlink.loader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BOMFileReader {
    public List<BomLinkData> readFile(File file)  {
        List<BomLinkData> dataList = new ArrayList<>();

        /*try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dataList.add(BomLinkData.fromCSV(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dataList.add(BomLinkData.fromCSV(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return dataList;
    }

    public List<BomLinkData> rFile(File file) {
        List<BomLinkData> dataList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dataList.add(BomLinkData.fromCSV(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataList;
    }
}