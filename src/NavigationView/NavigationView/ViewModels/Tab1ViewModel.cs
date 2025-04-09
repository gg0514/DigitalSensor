using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using NavigationView.Services;
using System.Collections.ObjectModel;
using System.Threading.Tasks;

namespace NavigationView.ViewModels;


public partial class Tab1ViewModel : ViewModelBase
{
    [ObservableProperty]
    private string _title = "Tab 1 Content";

    [ObservableProperty]
    private ObservableCollection<string> _tab1Items;

    public Tab1ViewModel()
    {
        Tab1Items = new ObservableCollection<string>
        {
            "Tab 1 - Item 1",
            "Tab 1 - Item 2",
            "Tab 1 - Item 3"
        };
    }
}