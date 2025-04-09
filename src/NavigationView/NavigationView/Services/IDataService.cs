using System.Collections.Generic;
using System.Threading.Tasks;

namespace NavigationView.Services;

public interface IDataService
{
    Task<List<string>> GetItemsAsync();
}