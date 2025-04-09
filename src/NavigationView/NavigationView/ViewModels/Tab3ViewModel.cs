using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using NavigationView.Services;
using System.Collections.ObjectModel;
using System.Threading.Tasks;

namespace NavigationView.ViewModels;


public partial class Tab3ViewModel : ViewModelBase
{
    [ObservableProperty]
    private string _title = "Tab 3 Content";

    [ObservableProperty]
    private string _newItemText = string.Empty;

    [ObservableProperty]
    private ObservableCollection<string> _tab3Items;

    public Tab3ViewModel()
    {
        Tab3Items = new ObservableCollection<string>
        {
            "Tab 3 - Item A",
            "Tab 3 - Item B"
        };
    }

    [RelayCommand]
    private void AddItem()
    {
        if (!string.IsNullOrWhiteSpace(NewItemText))
        {
            Tab3Items.Add(NewItemText);
            NewItemText = string.Empty;
        }
    }
}