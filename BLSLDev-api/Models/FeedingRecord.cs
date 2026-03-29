using System.ComponentModel.DataAnnotations;

namespace BLSLDev_api.Models
{
    public class FeedingRecord
    {
        [Key]
        public int Id { get; set; }
        
        [Required]
        public string RawMaterialCode { get; set; }
        
        [Required]
        public string BucketCode { get; set; }
        
        public string ValidationResult { get; set; }
        
        public DateTime OperationTime { get; set; }
    }
}
