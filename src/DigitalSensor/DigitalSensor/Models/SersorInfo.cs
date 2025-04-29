using Avalonia.Media;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;



namespace DigitalSensor.Models;

public class SensorInfo
{
    public SensorType Type { get; set; }
    public string Serial { get; set; }             // hexstring으로 표시
}

public enum SensorType
{
    [Description("None")]
    None = 0,
    [Description("pH")]
    PH = 1,
    ORP = 2,                // ORP 센서
    DO = 3,                 // 용존산소 센서    
    [Description("EC")]
    Conductivity = 5,       // 전도도 센서
    Ozone = 6,              // 오존 센서
    [Description("TU")]
    TurbidityLow = 7,
    [Description("TU")]
    TurbidityHighColor = 8,
    [Description("TU")]
    TurbidityHighIR = 9,
    [Description("CL")]
    Chlorine = 10,
    SuspendedSolids = 11    // 부유물질 센서
}

public enum CalibrationStatus
{
    [Description("대 기")]
    NoSensorCalibration = 0,
    [Description("진 행")]
    CalInProgress = 1,
    [Description("성 공")]
    CalOK = 2,
    FailNotStable = 3,
    FailBufferNotFound = 4,
    FailFirstBufferNotFound = 5,
    FailSecondBufferNotFound = 6,
    ValueTooLow = 7,
    FailValueTooHigh = 8,
    FailSlopeTooLow = 9,
    FailSlopeTooHigh = 10
}

public static class EnumExtensions
{
    public static string GetDescription(this Enum value)
    {
        FieldInfo field = value.GetType().GetField(value.ToString());
        DescriptionAttribute attribute = field.GetCustomAttribute<DescriptionAttribute>();
        return attribute == null ? value.ToString() : attribute.Description;
    }
}

// ** 참고
// record 은 C# 9.0 부터 지원하는 기능으로, record 의 의미는 아래와 같음
//
// public record Person(string name, int age);
// public class Person
// {
//    public string Name { get; init;  }
//    public int Age { get; init; }
//
//    public Person(string name, int age)
//    {
//        Name = name;
//        Age = age;
//    }
// }
