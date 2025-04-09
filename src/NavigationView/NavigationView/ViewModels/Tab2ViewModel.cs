using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using NavigationView.Services;
using System.Collections.ObjectModel;
using System.Threading.Tasks;

namespace NavigationView.ViewModels;

public partial class Tab2ViewModel : ViewModelBase
{
    [ObservableProperty]
    private string _title = "Tab 2 Content";

    [ObservableProperty]
    private string _description = "This is the content for Tab 2. It demonstrates a different tab view.";
}