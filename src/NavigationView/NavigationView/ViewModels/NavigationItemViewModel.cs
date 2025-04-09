using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace NavigationView.ViewModels;


public class NavigationItemViewModel
{
    public string Icon { get; set; } = string.Empty;
    public string Label { get; set; } = string.Empty;
    public ViewModelBase ViewModel { get; set; } = null!;
}


