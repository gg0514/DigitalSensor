using Avalonia.Platform;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.IO;


namespace DigitalSensor.Utils;


public class JsonLoader
{
    private static readonly string BasePath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "DigitalSensor");


    //public static JObject Load_modbusMap(string jsonFilePath)
    //{
    //    // 리소스 스트림 열기
    //    using var stream = AssetLoader.Open(new Uri($"avares://DigitalSensor/{jsonFilePath}"));
    //    using var reader = new StreamReader(stream);

    //    // JSON 문자열 읽기
    //    var jsonString = reader.ReadToEnd();
    //    return JObject.Parse(jsonString);
    //}

    private static JObject LoadFromFile(string fullPath)
    {
        string jsonString = File.ReadAllText(fullPath);
        return JObject.Parse(jsonString);
    }

    private static JObject LoadFromResource(string jsonFilePath)
    {
        using var stream = AssetLoader.Open(new Uri($"avares://DigitalSensor/Assets/{jsonFilePath}"));
        using var reader = new StreamReader(stream);
        string jsonString = reader.ReadToEnd();
        return JObject.Parse(jsonString);
    }


    public static JObject Load_modbusMap(string jsonFilePath)
    {
        if (string.IsNullOrWhiteSpace(jsonFilePath))
            throw new ArgumentException("File path cannot be empty or null.", nameof(jsonFilePath));

        // 내부 저장소에 파일이 없으면 리소스에서 로드
        JObject jObject = LoadFromResource(jsonFilePath);
        return jObject;
    }



    public static JObject Load_AppSettings(string jsonFilePath)
    {
        if (string.IsNullOrWhiteSpace(jsonFilePath))
            throw new ArgumentException("File path cannot be empty or null.", nameof(jsonFilePath));

        // 내부 저장소 경로
        string fullPath = Path.Combine(BasePath, Path.GetFileName(jsonFilePath));

        try
        {
            // 내부 저장소에 파일이 있는 경우 읽기
            if (File.Exists(fullPath))
            {
                return LoadFromFile(fullPath);
            }

            // 내부 저장소에 파일이 없으면 리소스에서 로드
            JObject jObject = LoadFromResource(jsonFilePath);

            // 리소스 데이터를 내부 저장소에 저장 (초기화)
            Save_toJson(jObject, jsonFilePath);

            return jObject;
        }
        catch (FileNotFoundException ex)
        {
            throw new FileNotFoundException($"JSON file not found: {jsonFilePath}", ex);
        }
        catch (IOException ex)
        {
            throw new IOException($"Failed to load JSON from file: {jsonFilePath}", ex);
        }
        catch (JsonReaderException ex)
        {
            throw new JsonReaderException($"Invalid JSON format in file: {jsonFilePath}", ex);
        }
    }



    public static void Save_toJson(JObject obj, string jsonFilePath)
    {
        if (obj == null)
            throw new ArgumentNullException(nameof(obj), "JObject cannot be null.");

        if (string.IsNullOrWhiteSpace(jsonFilePath))
            throw new ArgumentException("File path cannot be empty or null.", nameof(jsonFilePath));

        // JSON 문자열로 변환
        string jsonString = obj.ToString(Formatting.Indented);

        // 내부 저장소 경로 생성
        try
        {
            // 디렉토리 생성
            Directory.CreateDirectory(BasePath);

            // 파일 경로
            string fullPath = Path.Combine(BasePath, Path.GetFileName(jsonFilePath));

            // 파일에 저장
            File.WriteAllText(fullPath, jsonString);
        }
        catch (IOException ex)
        {
            throw new IOException($"Failed to save JSON to file: {jsonFilePath}", ex);
        }
    }


}
