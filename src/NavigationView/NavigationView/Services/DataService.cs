using System.Collections.Generic;
using System.Threading.Tasks;

namespace NavigationView.Services;

public class DataService : IDataService
{
    public async Task<List<string>> GetItemsAsync()
    {
        // Simulate API call
        await Task.Delay(500);
        return new List<string>
        {
            "Item 1",
            "Item 2",
            "Item 3",
            "Item 4",
            "Item 5"
        };
    }
}