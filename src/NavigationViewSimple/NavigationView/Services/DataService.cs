using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace NavigationView.Services;

public class DataService 
{
    public event Action<string> DataReceived;

    public DataService()
    {
        Task.Run(async () =>
        {
            while (true)
            {
                await Task.Delay(1000);
                var simulatedData = $"Data @ {DateTime.Now:T}";
                DataReceived?.Invoke(simulatedData);
            }
        });
    }
}