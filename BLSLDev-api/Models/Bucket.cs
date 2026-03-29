using System.ComponentModel.DataAnnotations;

namespace BLSLDev_api.Models
{
    public class Bucket
    {
        [Key]
        public int Id { get; set; }
        
        [Required]
        public string Code { get; set; }
        
        [Required]
        public string RawMaterialCode { get; set; }
        
        [Required]
        public string RawMaterialDesc { get; set; }
        
        [Required]
        public decimal Weight { get; set; }
        
        public string Status { get; set; }
        
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }
}
