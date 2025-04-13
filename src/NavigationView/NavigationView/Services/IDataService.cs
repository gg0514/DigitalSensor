using System;
using System.Reactive.Linq;


namespace NavigationView.Services;

public interface IDataService
{
    IObservable<string> GetInstrumentData();
}

public class DataService : IDataService
{
    public IObservable<string> GetInstrumentData()
    {
        return Observable.Interval(TimeSpan.FromSeconds(1))
                         .Select(i => $"Instrument Value: {i}");
    }
}