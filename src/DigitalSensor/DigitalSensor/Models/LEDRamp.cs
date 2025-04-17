using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DigitalSensor.Models
{
    public enum TxStatus
    {
        NoSignal = 0,
        Signal = 1
    }

    public enum RxStatus
    {
        NoSignal = 0,
        Signal = 1
    }

    public enum ErrStatus
    {
        Disconnected = 0,                // disconnected        
        Connected = 1                // connected
    }


    public class LEDRamp
    {
        public ErrStatus Err { get; set; } = ErrStatus.Disconnected;
        public TxStatus Tx { get; set; } = TxStatus.NoSignal;
        public RxStatus Rx { get; set; } = RxStatus.NoSignal;

        public string ErrColor => Err switch
        {
            ErrStatus.Disconnected => "Red",
            ErrStatus.Connected => "GreenYellow",
            _ => "Gray"
        };

        public string TxColor => Tx switch
        {
            TxStatus.NoSignal => "Gray",
            TxStatus.Signal => "GreenYellow",
            _ => "Gray"
        };

        public string RxColor => Rx switch
        {
            RxStatus.NoSignal => "Gray",
            RxStatus.Signal => "GreenYellow",
            _ => "Gray"
        };
    }
}
