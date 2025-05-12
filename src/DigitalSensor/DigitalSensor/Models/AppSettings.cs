using Avalonia.Media;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DigitalSensor.Models;
using Newtonsoft.Json.Linq;
using DigitalSensor.Utils;

namespace DigitalSensor.Models;


public class AppSettings
{
    public ModbusInfo ModbusInfo { get; set; } 
    public SerialConn SerialConn { get; set; } 
    public CalibrationAdjust CalibAdjust { get; set; }


    public AppSettings()
    {
        ModbusInfo = new ModbusInfo();
        SerialConn = new SerialConn();
        CalibAdjust = new CalibrationAdjust();
    }

    public AppSettings(JObject settings)
    {
        ModbusInfo = settings["ModbusInfo"].ToObject<ModbusInfo>();
        SerialConn = settings["SerialConn"].ToObject<SerialConn>();
        CalibAdjust = settings["CalibrationAdjust"].ToObject<CalibrationAdjust>();
    }

    public JObject ToJObject()
    {
        var settings = new JObject
        {
            ["ModbusInfo"] = JObject.FromObject(ModbusInfo),
            ["SerialConn"] = JObject.FromObject(SerialConn),
            ["CalibrationAdjust"] = JObject.FromObject(CalibAdjust)
        };
        return settings;
    }

    public void SaveSettings()
    {
        var settings = ToJObject();
        string filePath= "appsettings.json";

        JsonLoader.Save_toJson(settings, filePath);
    }
}


