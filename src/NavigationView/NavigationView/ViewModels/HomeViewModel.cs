using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using NavigationView.Services;
using System.Reactive.Linq;

namespace NavigationView.ViewModels;

public partial class HomeViewModel : ViewModelBase
{
    private readonly IDataService _dataService;

    [ObservableProperty]
    private string _instrumentData;

    public HomeViewModel(IDataService dataService)
    {
        //_dataService = dataService;
        //_dataService.GetInstrumentData()
        //    .Subscribe(data =>
        //    {
        //        InstrumentData = data;
        //    });

        _dataService = dataService;
        Observable.Return("Test").Subscribe(data => InstrumentData = data); // 간단한 테스트
        _dataService.GetInstrumentData().Subscribe(data => InstrumentData = data);

    }
}
