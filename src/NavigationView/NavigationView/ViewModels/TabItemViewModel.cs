
namespace NavigationView.ViewModels;

public class TabItemViewModel
{
    public string Header { get; set; } = string.Empty;
    public ViewModelBase Content { get; set; } = null!;
}