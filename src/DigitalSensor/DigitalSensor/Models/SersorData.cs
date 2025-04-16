using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DigitalSensor.Models
{

    public enum SensorType
    {
        None = 0,
        PH = 1,
        ORP = 2,                // ORP 센서
        DO = 3,                 // 용존산소 센서    
        Conductivity = 5,       // 전도도 센서
        Ozone = 6,              // 오존 센서
        TurbidityLow = 7, 
        TurbidityHighColor = 8, 
        TurbidityHighIR = 9,
        Chlorine = 10,
        SuspendedSolids = 11    // 부유물질 센서
    }


    public enum CalibrationStatus
    {
        NoSensorCalibration = 0,
        CalInProgress = 1,
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

    public record SensorInfo(   SensorType type, 
                                string serial);             // hexstring으로 표시


    public record SensorData(   float value,
                                float mv,
                                float temperature);

}



// ** 참고
// record 은 C# 9.0 부터 지원하는 기능으로, record 의 의미는 아래와 같음
//
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
