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
