
using CommunityToolkit.Mvvm.ComponentModel;


namespace DigitalSensor.Models;

public partial class SensorData : ObservableObject
{
    [ObservableProperty] private string _timestamp;
    [ObservableProperty] private float _value= 0;
    [ObservableProperty] private float _mv= 0;
    [ObservableProperty] private float _temperature = 0;
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
