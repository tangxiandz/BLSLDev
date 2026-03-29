using System.ComponentModel.DataAnnotations;

namespace BLSLDev_api.Models
{
    public class MixingRecord
    {
        [Key]
        public int Id { get; set; }
        
        [Required]
        public string SemiProductCode { get; set; }
        
        [Required]
        public string RawMaterialCode { get; set; }
        
        [Required]
        public string BucketCode { get; set; }
        
        public string Quantity { get; set; }
        
        public DateTime OperationTime { get; set; }
    }
}
