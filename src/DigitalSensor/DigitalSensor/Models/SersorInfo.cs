using Avalonia.Media;
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

    public enum TxStatus
    {
        NoSignal = 0,
        Signal = 1
    }

    public enum RxStatus
    {
        NoSignal = 0,
        Signal = 1
    }

    public enum ErrStatus
    {
        Disconnected = 0,                // disconnected        
        Connected = 1                // connected
    }


    public class SensorInfo
    {
        public ErrStatus Err { get; set; } = ErrStatus.Disconnected;
        public TxStatus  Tx  { get; set; } = TxStatus.NoSignal;
        public RxStatus  Rx  { get; set; } = RxStatus.NoSignal;

        public string ErrColor => Err switch
        {
            ErrStatus.Disconnected  => "Red",
            ErrStatus.Connected     => "GreenYellow",
            _ => "Gray"
        };

        public string TxColor => Tx switch
        {
            TxStatus.NoSignal   => "Gray",
            TxStatus.Signal     => "GreenYellow",
            _ => "Gray"
        };

        public string RxColor => Rx switch
        {
            RxStatus.NoSignal   => "Gray",
            RxStatus.Signal     => "GreenYellow",
            _ => "Gray"
        };

        public SensorType Type { get; set; }
        public string Serial { get; set; }             // hexstring으로 표시
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
